package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.DefinitionProvider;
import org.openforis.collect.android.IdGenerator;
import org.openforis.collect.android.SurveyException;
import org.openforis.collect.android.attributeconverter.AttributeConverter;
import org.openforis.collect.android.gui.util.meter.Timer;
import org.openforis.collect.android.util.persistence.Database;
import org.openforis.collect.android.viewmodel.*;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.model.*;
import org.openforis.collect.persistence.*;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.Value;
import org.openforis.idm.model.expression.ExpressionFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

// TODO: Break this up

/**
 * @author Daniel Wiell
 */
public class CollectModelManager implements DefinitionProvider, CodeListService {
    private final SurveyManager surveyManager;
    private final RecordManager recordManager;
    private final CodeListManager codeListManager;
    private final CodeListSizeEvaluator codeListSizeEvaluator;

    private final User user = new User();

    private RecordNodes recordNodes;
    private CollectSurvey selectedSurvey;
    private ModelConverter modelConverter;
    private Definitions definitions;

    private SpeciesManager speciesManager;

    public CollectModelManager(SurveyManager surveyManager,
                               RecordManager recordManager,
                               CodeListManager codeListManager,
                               Database database) {
        this.surveyManager = surveyManager;
        this.recordManager = recordManager;
        this.codeListManager = codeListManager;
        codeListSizeEvaluator = new CodeListSizeEvaluator(new DatabaseCodeListSizeDao(database));

        speciesManager = createSpeciesManager(database);
    }

    private SpeciesManager createSpeciesManager(Database database) {
        speciesManager = new SpeciesManager();
        TaxonDao taxonDao = new TaxonDao();
        taxonDao.setDataSource(database.dataSource());
        speciesManager.setTaxonDao(taxonDao);
        TaxonomyDao taxonomyDao = new TaxonomyDao();
        taxonomyDao.setDataSource(database.dataSource());
        speciesManager.setTaxonomyDao(taxonomyDao);
        TaxonVernacularNameDao taxonVernacularNameDao = new TaxonVernacularNameDao();
        taxonVernacularNameDao.setDataSource(database.dataSource());
        speciesManager.setTaxonVernacularNameDao(taxonVernacularNameDao);
        ExpressionFactory expressionFactory = new ExpressionFactory();
        expressionFactory.setLookupProvider(new DatabaseLookupProvider());
        speciesManager.setExpressionFactory(expressionFactory);
        return speciesManager; // TODO: Shouldn't be here - separate class, and provide as dependency
    }

    public UiSurvey importSurvey(InputStream inputStream) {
        try {
            CollectSurvey collectSurvey = surveyManager.importModel(inputStream, "survey", false);
            selectSurvey(collectSurvey);
            return modelConverter.toUiSurvey();
        } catch (SurveyImportException e) {
            throw new SurveyException(e);
        } catch (SurveyValidationException e) {
            throw new SurveyException(e);
        }
    }

    public UiSurvey loadSurvey(final String name) {
        CollectSurvey collectSurvey = Timer.time(SurveyDao.class, "loadSurvey", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                return surveyManager.getSurveyDao().load(name);
            }
        });
        if (collectSurvey == null)
            return null;
        selectSurvey(collectSurvey);
        return modelConverter.toUiSurvey();
    }

    public UiRecord addRecord(String entityName, UiSurvey survey) {
        try {
            CollectRecord record = recordManager.create(selectedSurvey, entityName, user, latestSurveyVersion());
            UiRecord uiRecord = modelConverter.toUiRecord(record, survey);
            recordNodes = new RecordNodes(record);
            return uiRecord;
        } catch (RecordPersistenceException e) {
            throw new SurveyException(e);
        }
    }

    public UiEntity addEntity(final UiEntityCollection uiEntityCollection) {
        return Timer.time(CollectModelManager.class, "addEntity", new Callable<UiEntity>() {
            public UiEntity call() throws Exception {
                Entity parentEntity = recordNodes.getEntityById(uiEntityCollection.getParentEntityId());
                NodeChangeSet changeSet = recordManager.addEntity(parentEntity, uiEntityCollection.getName());
                // TODO: Validation errors?
                Entity entity = extractAddedEntity(changeSet);
                UiEntity uiEntity = modelConverter.toUiEntity(selectedSurvey, entity, uiEntityCollection);
                recordNodes.add(entity);
                return uiEntity;
            }
        });
    }

    public UiAttribute addAttribute(UiAttributeCollection uiAttributeCollection) {
        Entity parentEntity = recordNodes.getEntityById(uiAttributeCollection.getParentEntityId());
        UiAttributeCollectionDefinition definition = uiAttributeCollection.getDefinition();
        String attributeName = definition.attributeDefinition.name;

        Value value = null; // TODO: Default value
        NodeChangeSet changeSet = recordManager.addAttribute(parentEntity, attributeName, value, null, null);
        // TODO: Validation errors?
        Attribute attribute = extractAddedAttribute(changeSet);
        attribute.setId(IdGenerator.nextId()); // TODO: Not right place to do this - use converter?
        recordNodes.add(attribute);
        return AttributeConverter.toUiAttribute(definition, attribute);
    }

    @SuppressWarnings("unchecked")
    public Set<UiValidationError> updateAttribute(UiAttribute uiAttribute) {
        Attribute attribute = recordNodes.getAttribute(uiAttribute.getId());
        Value value = AttributeConverter.toValue(uiAttribute);
        NodeChangeSet nodeChangeSet = recordManager.updateAttribute(attribute, value);
        return new NodeChangeSetParser(nodeChangeSet, attribute, uiAttribute).parseErrors();
    }

    public void recordSelected(UiRecord uiRecord) {
        CollectRecord record = modelConverter.toCollectRecord(uiRecord, selectedSurvey);
        recordNodes = new RecordNodes(record);
    }

    public Definition getById(String definitionId) {
        return definitions.definitionById(definitionId);
    }

    public List<UiCode> codeList(UiCodeAttribute uiAttribute) {
        CodeAttribute attribute = recordNodes.getCodeAttribute(uiAttribute.getId());
        List<CodeListItem> items = codeListManager.loadValidItems(attribute.getParent(), attribute.getDefinition());
        return modelConverter.toUiCodes(items);
    }

    public boolean isParentCodeAttribute(UiAttribute attribute, UiCodeAttribute codeAttribute) {
        if (!(attribute instanceof UiCodeAttribute))
            return false;
        CodeAttributeDefinition definition = (CodeAttributeDefinition) getDefinition(codeAttribute);
        CodeAttributeDefinition parentDefinition = definition.getParentCodeAttributeDefinition();
        return parentDefinition != null && parentDefinition.getId() == getDefinition(attribute).getId();
    }

    public int getMaxCodeListSize(UiCodeAttribute uiAttribute) {
        CodeAttribute attribute = recordNodes.getCodeAttribute(uiAttribute.getId());
        return codeListSizeEvaluator.size(attribute.getDefinition());
    }

    private AttributeDefinition getDefinition(UiAttribute uiAttribute) {
        int definitionId = Integer.parseInt(uiAttribute.getDefinition().id);
        return (AttributeDefinition) selectedSurvey.getSchema().getDefinitionById(definitionId);
    }

    private void selectSurvey(CollectSurvey survey) {
        selectedSurvey = survey;
        definitions = new Definitions(selectedSurvey);
        modelConverter = new ModelConverter(selectedSurvey, definitions);
    }

    private Entity extractAddedEntity(NodeChangeSet changeSet) {
        for (NodeChange<?> nodeChange : changeSet.getChanges()) {
            if (nodeChange instanceof EntityAddChange)
                return ((EntityAddChange) nodeChange).getNode();
        }
        throw new IllegalStateException("No " + EntityAddChange.class.getName() + " found in change set");
    }

    private Attribute extractAddedAttribute(NodeChangeSet changeSet) {
        for (NodeChange<?> nodeChange : changeSet.getChanges()) {
            if (nodeChange instanceof AttributeAddChange)
                return ((AttributeAddChange) nodeChange).getNode();
        }
        throw new IllegalStateException("No " + EntityAddChange.class.getName() + " found in change set");
    }

    private String latestSurveyVersion() {
        List<ModelVersion> versions = selectedSurvey.getVersions();
        if (versions == null || versions.isEmpty())
            return null;
        return versions.get(versions.size() - 1).getName();
    }
}