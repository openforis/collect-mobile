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
import org.openforis.idm.model.*;
import org.openforis.idm.model.expression.ExpressionFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

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

    public UiSurvey loadSurvey() {
        CollectSurvey collectSurvey = Timer.time(SurveyDao.class, "loadSurvey", new Callable<CollectSurvey>() {
            public CollectSurvey call() throws Exception {
                SurveyDao surveyDao = surveyManager.getSurveyDao();
                List<SurveySummary> surveySummaries = surveyDao.loadSummaries();
                if (surveySummaries == null || surveySummaries.isEmpty())
                    return null;
                SurveySummary surveySummary = surveySummaries.get(0);
                return surveyDao.load(surveySummary.getName());
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
                Entity entity = extractAddedEntity(changeSet);
                UiEntity uiEntity = modelConverter.toUiEntity(selectedSurvey, entity, uiEntityCollection);
                recordNodes.add(entity);
                return uiEntity;
            }
        });
    }

    public UiAttribute addAttribute(UiAttributeCollection uiAttributeCollection) {
        Entity parentEntity = recordNodes.getEntityById(uiAttributeCollection.getParentEntityId());
        Definition definition = uiAttributeCollection.getDefinition().attributeDefinition;

        Value value = null; // TODO: Set the default value
        NodeChangeSet changeSet = recordManager.addAttribute(parentEntity, definition.name, value, null, null);
        Attribute attribute = extractAddedAttribute(changeSet);
        attribute.setId(IdGenerator.nextId()); // TODO: Not right place to do this - use converter?
        recordNodes.add(attribute);
        return AttributeConverter.toUiAttribute(definition, attribute);
    }

    @SuppressWarnings("unchecked")
    public Map<UiAttribute, UiAttributeChange> updateAttribute(final UiAttribute uiAttribute) {
        // TODO: Remove timing
        return Timer.time(CollectModelManager.class, "updateAttribute(" + uiAttribute + ")", new Callable<Map<UiAttribute, UiAttributeChange>>() {
            public Map<UiAttribute, UiAttributeChange> call() throws Exception {
                Attribute attribute = recordNodes.getAttribute(uiAttribute.getId());
                Value value = AttributeConverter.toValue(uiAttribute);
                NodeChangeSet nodeChangeSet = recordManager.updateAttribute(attribute, value);
                // TODO: This shouldn't be called attributeChanges, it contains the updates
                Map<UiAttribute, UiAttributeChange> attributeChanges = new NodeChangeSetParser(nodeChangeSet, uiAttribute.getUiRecord()).extractChanges();
                if (uiAttribute instanceof UiCodeAttribute)
                    updateChildrenCodeAttributes((UiCodeAttribute) uiAttribute, attributeChanges.keySet());
                return attributeChanges;
            }
        });
    }

    /**
     * Update children attribute codes with correct label.
     */
    private void updateChildrenCodeAttributes(UiCodeAttribute uiCodeAttribute, Collection<UiAttribute> uiAttributes) {
        int parentDefinitionId = Integer.parseInt(uiCodeAttribute.getDefinition().id);
        for (UiAttribute uiAttribute : uiAttributes) {
            if (uiAttribute instanceof UiCodeAttribute) {
                CodeAttributeDefinition nodeDefinition = (CodeAttributeDefinition) selectedSurvey.getSchema().getDefinitionById(Integer.parseInt(uiAttribute.getDefinition().id));
                CodeAttributeDefinition parentDefinition = nodeDefinition.getParentCodeAttributeDefinition();
                if (parentDefinition != null && parentDefinition.getId() == parentDefinitionId) {
                    CodeAttribute childCodeAttribute = recordNodes.getCodeAttribute(uiAttribute.getId());
                    CodeListItem item = codeListManager.loadItemByAttribute(childCodeAttribute);
                    if (item != null) {
                        UiCode updatedCode = new UiCode(item.getCode(), item.getLabel());
                        ((UiCodeAttribute) uiAttribute).setCode(updatedCode);
                    }
                }
            }
        }
    }

    public void removeAttribute(UiAttribute uiAttribute) {
        Attribute attribute = recordNodes.getAttribute(uiAttribute.getId());
        recordManager.deleteNode(attribute);
        recordNodes.remove(uiAttribute.getId());
    }

    public void removeEntity(int entityId) {
        Node node = recordNodes.getEntityById(entityId);
        recordManager.deleteNode(node);
        recordNodes.remove(entityId);
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

    public List<UiCode> codeList(UiAttributeCollection uiAttributeCollection) {
        if (!uiAttributeCollection.getDefinition().isOfAttributeType(UiCodeAttribute.class))
            throw new IllegalStateException("uiAttributeCollection " + uiAttributeCollection + " expected to have UiAttributeCollection attribute type");
        Entity parentEntity = recordNodes.getEntityById(uiAttributeCollection.getParentEntityId());
        Definition definition = uiAttributeCollection.getDefinition().attributeDefinition;
        CodeAttributeDefinition codeAttributeDefinition = (CodeAttributeDefinition) selectedSurvey.getSchema().getDefinitionById(Integer.parseInt(definition.id));
        List<CodeListItem> items = codeListManager.loadValidItems(parentEntity, codeAttributeDefinition);
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


    public int getMaxCodeListSize(Definition definition) {
        return codeListSizeEvaluator.size((CodeAttributeDefinition) selectedSurvey.getSchema().getDefinitionById(Integer.parseInt(definition.id)));
    }

    public CollectRecord getCollectRecord(int recordId) {
        Entity rootEntity = recordNodes.getEntityById(recordId);
        CollectRecord collectRecord = new CollectRecord(selectedSurvey, latestSurveyVersion());
        collectRecord.setId(recordId);
        collectRecord.setRootEntity(rootEntity);
        return collectRecord;
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

    public void exportSurvey(UiSurvey uiSurvey, java.io.File exportFile, final ExportListener exportListener) throws IOException {
        new SurveyExporter(uiSurvey, selectedSurvey, surveyManager, new SurveyExporter.CollectRecordProvider() {
            public CollectRecord record(int recordId) {
                exportListener.beforeRecordExport(recordId);
                return getCollectRecord(recordId);
            }
        }).export(exportFile);

    }

    public interface ExportListener {
        void beforeRecordExport(int recordId);
    }
}