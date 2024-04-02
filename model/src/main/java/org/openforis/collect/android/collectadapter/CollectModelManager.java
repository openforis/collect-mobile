package org.openforis.collect.android.collectadapter;

import org.jooq.SQLDialect;
import org.jooq.impl.DefaultConfiguration;
import org.openforis.collect.android.CodeListService;
import org.openforis.collect.android.CoordinateDestinationService;
import org.openforis.collect.android.DefinitionProvider;
import org.openforis.collect.android.IdGenerator;
import org.openforis.collect.android.Settings;
import org.openforis.collect.android.SurveyDataExportParameters;
import org.openforis.collect.android.SurveyException;
import org.openforis.collect.android.attributeconverter.AttributeConverter;
import org.openforis.collect.android.gui.util.meter.Timer;
import org.openforis.collect.android.util.CoordinateUtils;
import org.openforis.collect.android.util.persistence.Database;
import org.openforis.collect.android.viewmodel.Definition;
import org.openforis.collect.android.viewmodel.UiAttribute;
import org.openforis.collect.android.viewmodel.UiAttributeCollection;
import org.openforis.collect.android.viewmodel.UiAttributeDefinition;
import org.openforis.collect.android.viewmodel.UiCode;
import org.openforis.collect.android.viewmodel.UiCodeAttribute;
import org.openforis.collect.android.viewmodel.UiCodeList;
import org.openforis.collect.android.viewmodel.UiCoordinateAttribute;
import org.openforis.collect.android.viewmodel.UiEntity;
import org.openforis.collect.android.viewmodel.UiEntityCollection;
import org.openforis.collect.android.viewmodel.UiFileAttribute;
import org.openforis.collect.android.viewmodel.UiInternalNode;
import org.openforis.collect.android.viewmodel.UiNode;
import org.openforis.collect.android.viewmodel.UiNodeChange;
import org.openforis.collect.android.viewmodel.UiRecord;
import org.openforis.collect.android.viewmodel.UiSpatialReferenceSystem;
import org.openforis.collect.android.viewmodel.UiSurvey;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.manager.RecordFileManager;
import org.openforis.collect.manager.RecordManager;
import org.openforis.collect.manager.SpeciesManager;
import org.openforis.collect.manager.SurveyManager;
import org.openforis.collect.manager.exception.SurveyValidationException;
import org.openforis.collect.metamodel.CollectAnnotations;
import org.openforis.collect.model.AttributeAddChange;
import org.openforis.collect.model.CollectRecord;
import org.openforis.collect.model.CollectSurvey;
import org.openforis.collect.model.EntityAddChange;
import org.openforis.collect.model.NodeChange;
import org.openforis.collect.model.NodeChangeMap;
import org.openforis.collect.model.NodeChangeSet;
import org.openforis.collect.model.SurveyFile;
import org.openforis.collect.model.User;
import org.openforis.collect.persistence.SurveyDao;
import org.openforis.collect.persistence.SurveyImportException;
import org.openforis.collect.persistence.jooq.CollectDSLContext;
import org.openforis.commons.collection.CollectionUtils;
import org.openforis.commons.collection.Predicate;
import org.openforis.idm.metamodel.AttributeDefinition;
import org.openforis.idm.metamodel.CodeAttributeDefinition;
import org.openforis.idm.metamodel.CodeListItem;
import org.openforis.idm.metamodel.FileAttributeDefinition;
import org.openforis.idm.metamodel.ModelVersion;
import org.openforis.idm.metamodel.validation.Check;
import org.openforis.idm.metamodel.validation.DistanceCheck;
import org.openforis.idm.metamodel.validation.ValidationResultFlag;
import org.openforis.idm.metamodel.validation.ValidationResults;
import org.openforis.idm.metamodel.validation.Validator;
import org.openforis.idm.model.Attribute;
import org.openforis.idm.model.CodeAttribute;
import org.openforis.idm.model.Coordinate;
import org.openforis.idm.model.CoordinateAttribute;
import org.openforis.idm.model.Entity;
import org.openforis.idm.model.FileAttribute;
import org.openforis.idm.model.Node;
import org.openforis.idm.model.NodePointer;
import org.openforis.idm.model.Value;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * @author Daniel Wiell
 */
public class CollectModelManager implements DefinitionProvider, CodeListService, CoordinateDestinationService {

    private static final String FILE_TYPE_IMAGE_EXTENSION = "jpg";
    private static final String FILE_TYPE_AUDIO_EXTENSION = "3gp";
    private static final String FILE_TYPE_VIDEO_EXTENSION = "mp4";

    private final SurveyManager surveyManager;
    private final RecordManager recordManager;
    private final CodeListManager codeListManager;
    private final SpeciesManager speciesManager;
    private final RecordFileManager recordFileManager;
    private final CodeListSizeEvaluator codeListSizeEvaluator;

    private final User user = new User();
    private final CollectDSLContext jooqDsl;

    private Settings.PreferredLanguageMode languagePreference;
    private String preferredLanguage;
    private String selectedSurveyPreferredLanguage;
    private RecordNodes recordNodes;
    private CollectSurvey selectedSurvey;
    private boolean selectedSurveyHasGuide;
    private ModelConverter modelConverter;
    private Definitions definitions;

    public CollectModelManager(SurveyManager surveyManager,
                               RecordManager recordManager,
                               CodeListManager codeListManager,
                               SpeciesManager speciesManager,
                               RecordFileManager recordFileManager,
                               Database database,
                               Settings.PreferredLanguageMode languagePreference,
                               String preferredLanguage) {
        this.surveyManager = surveyManager;
        this.recordManager = recordManager;
        this.codeListManager = codeListManager;
        this.speciesManager = speciesManager;
        this.recordFileManager = recordFileManager;
        this.languagePreference = languagePreference;
        this.preferredLanguage = preferredLanguage;

        codeListSizeEvaluator = new CodeListSizeEvaluator(new DatabaseCodeListSizeDao(database));

        DefaultConfiguration jooqConfig = new DefaultConfiguration();
        jooqConfig.setSettings(jooqConfig.settings().withRenderSchema(false));
        jooqConfig
                .set(database.dataSource())
                .set(SQLDialect.SQLITE);
        jooqDsl = new CollectDSLContext(jooqConfig);
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
        try {
            codeListSizeEvaluator.reset();
            CollectSurvey collectSurvey = Timer.time(SurveyDao.class, "loadSurvey", new Callable<CollectSurvey>() {
                public CollectSurvey call() throws Exception {
                    SurveyDao surveyDao = surveyManager.getSurveyDao();
                    List<CollectSurvey> surveys = surveyDao.loadAll();
                    if (surveys.isEmpty())
                        return null;
                    return surveys.get(0);
                }
            });
            if (collectSurvey == null)
                return null;
            selectSurvey(collectSurvey);
            return modelConverter.toUiSurvey();
        } catch (Exception e) {
            throw new SurveyException(e);
        }
    }

    public UiRecord addRecord(String entityName, UiSurvey survey) {
        CollectRecord record = recordManager.create(selectedSurvey, entityName, user, latestSurveyVersion(), null, CollectRecord.Step.CLEANSING);
        UiRecord uiRecord = modelConverter.toUiRecord(record, survey);
        uiRecord.setNewRecord(true);
        recordNodes = new RecordNodes(record);
        return uiRecord;
    }

    public NodeAddedResult<UiEntity> addEntity(final UiEntityCollection uiEntityCollection) {
        Entity parentEntity = recordNodes.getEntityById(uiEntityCollection.getParentEntityId());
        NodeChangeSet changeSet = recordManager.addEntity(parentEntity, uiEntityCollection.getName());
        Entity entity = extractAddedEntity(changeSet);
        UiEntity uiEntity = modelConverter.toUiEntity(selectedSurvey, entity, uiEntityCollection);
        recordNodes.add(entity);
        Map<UiNode, UiNodeChange> nodeChanges = new NodeChangeSetParser(changeSet, uiEntity.getUiRecord(), selectedSurveyPreferredLanguage).extractChanges();
        return new NodeAddedResult<UiEntity>(uiEntity, nodeChanges);
    }

    public NodeAddedResult<UiAttribute> addAttribute(UiAttributeCollection uiAttributeCollection) {
        Entity parentEntity = recordNodes.getEntityById(uiAttributeCollection.getParentEntityId());
        UiAttributeDefinition definition = uiAttributeCollection.getDefinition().attributeDefinition;

        Value value = null; // TODO: Set the default value
        NodeChangeSet changeSet = recordManager.addAttribute(parentEntity, definition.name, value, null, null);
        Attribute attribute = extractAddedAttribute(changeSet);
        attribute.setId(IdGenerator.nextId()); // TODO: Not right place to do this - use converter?
        recordNodes.add(attribute);
        UiAttribute uiAttribute = AttributeConverter.toUiAttribute(definition, attribute);
        Map<UiNode, UiNodeChange> nodeChanges = new NodeChangeSetParser(changeSet, uiAttributeCollection.getUiRecord(), selectedSurveyPreferredLanguage).extractChanges();
        return new NodeAddedResult<UiAttribute>(uiAttribute, nodeChanges);
    }

    @SuppressWarnings("unchecked")
    public Map<UiNode, UiNodeChange> updateAttribute(final UiAttribute uiAttribute) {
        Attribute attribute = recordNodes.getAttribute(uiAttribute.getId());
        Value value = AttributeConverter.toValue(uiAttribute);
        NodeChangeSet nodeChangeSet = recordManager.updateAttribute(attribute, value);
        Map<UiNode, UiNodeChange> nodeChanges = new NodeChangeSetParser(nodeChangeSet, uiAttribute.getUiRecord(), selectedSurveyPreferredLanguage).extractChanges();
        if (uiAttribute instanceof UiCodeAttribute)
            updateChildrenCodeAttributes((UiCodeAttribute) uiAttribute, nodeChanges.keySet());
        return nodeChanges;
    }

    public Map<UiNode, UiNodeChange> validateAttribute(UiAttribute uiAttribute) {
        Attribute attribute = recordNodes.getAttribute(uiAttribute.getId());
        Validator validator = attribute.getRecord().getSurveyContext().getValidator();
        ValidationResults attributeResult = validator.validate(attribute);
        ValidationResultFlag cardinalityResult = validator.validateMinCount(attribute.getParent(), attribute.getName());
        NodeChangeMap changeMap = new NodeChangeMap();
        changeMap.addMinCountValidationResultChange(new NodePointer(attribute), cardinalityResult);
        changeMap.addValidationResultChange(attribute, attributeResult);
        return new NodeChangeSetParser(changeMap, uiAttribute.getUiRecord(), selectedSurveyPreferredLanguage).extractChanges();
    }

    public Map<UiNode, UiNodeChange> validateEntityCollection(UiEntityCollection entityCollection) {
        Entity parentEntity = recordNodes.getEntityById(entityCollection.getParentEntityId());
        Validator validator = parentEntity.getRecord().getSurveyContext().getValidator();
        ValidationResultFlag cardinalityResult = validator.validateMinCount(parentEntity, entityCollection.getName());
        NodeChangeMap changeMap = new NodeChangeMap();
        changeMap.addMinCountValidationResultChange(new NodePointer(parentEntity, entityCollection.getName()), cardinalityResult);
        return new NodeChangeSetParser(changeMap, entityCollection.getUiRecord(), selectedSurveyPreferredLanguage).extractChanges();
    }

    /**
     * Update children attribute codes with correct label.
     */
    private void updateChildrenCodeAttributes(UiCodeAttribute uiCodeAttribute, Collection<UiNode> uiNodes) {
        int parentDefinitionId = Integer.parseInt(uiCodeAttribute.getDefinition().id);
        for (UiNode uiNode : uiNodes) {
            if (uiNode instanceof UiCodeAttribute) {
                CodeAttributeDefinition nodeDefinition = selectedSurvey.getSchema().getDefinitionById(Integer.parseInt(uiNode.getDefinition().id));
                CodeAttributeDefinition parentDefinition = nodeDefinition.getParentCodeAttributeDefinition();
                if (parentDefinition != null && parentDefinition.getId() == parentDefinitionId) {
                    CodeAttribute childCodeAttribute = recordNodes.getCodeAttribute(uiNode.getId());
                    CodeListItem item = codeListManager.loadItemByAttribute(childCodeAttribute);
                    if (item != null) {
                        UiCodeAttribute childUiCodeAttribute = (UiCodeAttribute) uiNode;
                        UiCode updatedCode = new UiCode(item.getCode(), item.getLabel(preferredLanguage, true), null,
                                childUiCodeAttribute.getDefinition().isValueShown());
                        childUiCodeAttribute.setCode(updatedCode);
                    }
                }
            }
        }
    }

    public Map<UiNode, UiNodeChange> removeAttribute(UiAttribute uiAttribute) {
        Attribute attribute = recordNodes.getAttribute(uiAttribute.getId());
        NodeChangeSet nodeChangeSet = recordManager.deleteNode(attribute);
        recordNodes.remove(uiAttribute.getId());
        return new NodeChangeSetParser(nodeChangeSet, uiAttribute.getUiRecord(), selectedSurveyPreferredLanguage).extractChanges();
    }

    public Map<UiNode, UiNodeChange> removeEntity(UiEntity uiEntity) {
        Node node = recordNodes.getEntityById(uiEntity.getId());
        NodeChangeSet nodeChangeSet = recordManager.deleteNode(node);
        recordNodes.remove(uiEntity.getId());
        return new NodeChangeSetParser(nodeChangeSet, uiEntity.getUiRecord(), selectedSurveyPreferredLanguage).extractChanges();
    }

    public CollectRecord toCollectRecord(UiRecord uiRecord, boolean recordWillBeUpdated) {
        return modelConverter.toCollectRecord(uiRecord, selectedSurvey, recordWillBeUpdated);
    }

    public UiNode toUiNode(Node node, UiRecord uiRecord) {
        Entity parent = node.getParent();
        if (parent == null) return null;
        UiNode parentUiNode = uiRecord.lookupNode(parent.getId());
        if (parentUiNode == null) return null;

        if (node.getDefinition().isMultiple()) {
            // parent ui node is a node collection
            parentUiNode = ((UiInternalNode) parentUiNode).getChildByDefId(node.getDefinition().getId());
        }
        UiNode uiNode = null;
        if (node instanceof Entity && parentUiNode instanceof UiEntityCollection) {
            uiNode = modelConverter.toUiEntity(selectedSurvey, (Entity) node, (UiEntityCollection) parentUiNode);
        } else if (node instanceof Attribute && parentUiNode instanceof UiEntity) {
            uiNode = modelConverter.toUiAttribute(selectedSurvey, (Attribute<?, ?>) node, (UiEntity) parentUiNode);
        }
        if (uiNode == null) {
            return null;
        } else {
            recordNodes.add(node);
            return uiNode;
        }
    }

    public void recordSelected(CollectRecord record) {
        recordNodes = new RecordNodes(record);
    }

    public Definition getById(String definitionId) {
        Definition definition = definitions.definitionById(definitionId);
        if (definition == null)
            throw new IllegalArgumentException("No definition exists with id " + definitionId);
        return definition;
    }

    public UiCodeList codeList(UiCodeAttribute uiAttribute) {
        CodeAttribute attribute = recordNodes.getCodeAttribute(uiAttribute.getId());
        List<CodeListItem> items = codeListManager.loadValidItems(attribute.getParent(), attribute.getDefinition());
        boolean valueShown = selectedSurvey.getUIOptions().getShowCode(attribute.getDefinition());
        return modelConverter.toUiCodeList(items, valueShown, selectedSurveyPreferredLanguage);
    }

    public UiCodeList codeList(UiAttributeCollection uiAttributeCollection) {
        if (!uiAttributeCollection.getDefinition().isOfAttributeType(UiCodeAttribute.class))
            throw new IllegalStateException("uiAttributeCollection " + uiAttributeCollection + " expected to have UiAttributeCollection attribute type");
        Entity parentEntity = recordNodes.getEntityById(uiAttributeCollection.getParentEntityId());
        Definition definition = uiAttributeCollection.getDefinition().attributeDefinition;
        CodeAttributeDefinition codeAttributeDefinition = selectedSurvey.getSchema().getDefinitionById(Integer.parseInt(definition.id));
        List<CodeListItem> items = codeListManager.loadValidItems(parentEntity, codeAttributeDefinition);
        boolean valueShown = selectedSurvey.getUIOptions().getShowCode(codeAttributeDefinition);
        return modelConverter.toUiCodeList(items, valueShown, selectedSurveyPreferredLanguage);
    }

    public UiCode codeListItem(UiCodeAttribute uiCodeAttribute) {
        CodeAttribute attribute = recordNodes.getCodeAttribute(uiCodeAttribute.getId());
        CodeListItem item = codeListManager.loadItemByAttribute(attribute);
        if (item == null) return null;
        boolean valueShown = selectedSurvey.getUIOptions().getShowCode(attribute.getDefinition());
        return modelConverter.toUiCode(item, valueShown, selectedSurveyPreferredLanguage);
    }

    public boolean isParentCodeAttribute(UiAttribute attribute, UiCodeAttribute codeAttribute) {
        if (!(attribute instanceof UiCodeAttribute))
            return false;
        CodeAttributeDefinition definition = (CodeAttributeDefinition) getDefinition(codeAttribute);
        CodeAttributeDefinition parentDefinition = definition.getParentCodeAttributeDefinition();
        return parentDefinition != null && parentDefinition.getId() == getDefinition(attribute).getId();
    }

    public boolean isParentCodeAttribute(UiAttribute attribute, UiAttributeCollection attributeCollection) {
        if (!(attribute instanceof UiCodeAttribute) || !UiCodeAttribute.class.isAssignableFrom(attributeCollection.getDefinition().attributeType))
            return false;
        CodeAttributeDefinition definition = (CodeAttributeDefinition) getDefinition(attributeCollection);
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

    private AttributeDefinition getDefinition(UiAttribute uiAttribute) {
        int definitionId = Integer.parseInt(uiAttribute.getDefinition().id);
        return (AttributeDefinition) selectedSurvey.getSchema().getDefinitionById(definitionId);
    }

    private AttributeDefinition getDefinition(UiAttributeCollection attributeCollection) {
        int definitionId = Definitions.extractOriginalDefinitionId(attributeCollection.getDefinition());
        return (AttributeDefinition) selectedSurvey.getSchema().getDefinitionById(definitionId);
    }

    private void selectSurvey(CollectSurvey survey) {
        selectedSurvey = survey;
        selectedSurveyHasGuide = getSurveyFileGuide() != null;
        selectedSurveyPreferredLanguage = determineSelectedSurveyPreferredLanguage();
        definitions = new Definitions(selectedSurvey, selectedSurveyPreferredLanguage);
        modelConverter = new ModelConverter(selectedSurvey, definitions);
    }

    private String determineSelectedSurveyPreferredLanguage() {
        String lang;
        switch (languagePreference) {
            case SURVEY_DEFAULT:
                lang = selectedSurvey.getDefaultLanguage();
                break;
            case SPECIFIED:
                lang = preferredLanguage;
                break;
            case SYSTEM_DEFAULT:
            default:
                lang = Locale.getDefault().getLanguage();
        }
        return selectedSurvey.getLanguages().contains(lang)
                ? lang
                : selectedSurvey.getDefaultLanguage();
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

    public void exportSurvey(UiSurvey uiSurvey, File exportFile, SurveyDataExportParameters parameters,
                             final ExportListener exportListener) throws IOException {
        new SurveyExporter(surveyManager, new SurveyExporter.CollectRecordProvider() {
            public CollectRecord record(int recordId) {
                exportListener.beforeRecordExport(recordId);
                return getCollectRecordForExporting(recordId);
            }
        }, recordFileManager, uiSurvey, selectedSurvey, parameters).export(exportFile);
    }

    private CollectRecord getCollectRecordForExporting(int recordId) {
        Entity rootEntity = recordNodes.getEntityById(recordId);
        CollectRecord collectRecord = new CollectRecord(selectedSurvey, latestSurveyVersion(), rootEntity.getName(), false);
        collectRecord.setId(recordId);
        try {
            collectRecord.replaceRootEntity(rootEntity);
        } catch (Exception e) {
            // TODO to be fixed in collect-core
        }
        return collectRecord;
    }

    public File file(UiFileAttribute uiFileAttribute) {
        FileAttribute attribute = (FileAttribute) recordNodes.getAttribute(uiFileAttribute.getId());
        FileAttributeDefinition def = attribute.getDefinition();
        String extension = determineFileAttributeExtension(def);
        File dir = new File(recordFileManager.getDefaultStorageDirectory().getPath() + "/" + RecordFileManager.getRepositoryRelativePath(def));
        String fileName = org.apache.commons.lang3.StringUtils.isEmpty(attribute.getFilename())
                ? String.format("%s.%s", UUID.randomUUID(), extension)
                : attribute.getFilename(); // For backwards compatibility - previously internal ids was incorrectly used in filename
        return new File(dir, fileName);
    }

    private String determineFileAttributeExtension(FileAttributeDefinition def) {
        CollectAnnotations.FileType fileType = ((CollectSurvey) def.getSurvey()).getAnnotations().getFileType(def);
        switch (fileType) {
            case IMAGE:
                return FILE_TYPE_IMAGE_EXTENSION;
            case AUDIO:
                return FILE_TYPE_AUDIO_EXTENSION;
            case VIDEO:
                return FILE_TYPE_VIDEO_EXTENSION;
            default:
                return null;
        }
    }

    public double[] destination(UiCoordinateAttribute uiAttribute, double[] coordinate) {
        CoordinateAttribute attribute = (CoordinateAttribute) recordNodes.getAttribute(uiAttribute.getId());
        Coordinate previousValue = attribute.getValue();
        try {
            UiSpatialReferenceSystem srs = getUiSpatialReferenceSystem(uiAttribute);
            attribute.setValue(new Coordinate(coordinate[0], coordinate[1], srs.id));
            for (Check<?> check : attribute.getDefinition().getChecks()) {
                if (check instanceof DistanceCheck) {
                    Coordinate destinationPoint = ((DistanceCheck) check).evaluateDestinationPoint(attribute);
                    if (destinationPoint == null)
                        return null;
                    return CoordinateUtils.transform(
                            srs,
                            new double[]{destinationPoint.getX(), destinationPoint.getY()},
                            UiSpatialReferenceSystem.LAT_LNG_SRS
                    );

                }
            }
            throw new IllegalStateException("No distance check for " + uiAttribute);
        } finally {
            attribute.setValue(previousValue);
        }
    }

    private UiSpatialReferenceSystem getUiSpatialReferenceSystem(UiCoordinateAttribute uiAttribute) {
        UiSpatialReferenceSystem srs = uiAttribute.getSpatialReferenceSystem();
        // default to first srs defined
        return srs == null ? uiAttribute.getDefinition().spatialReferenceSystems.get(0) : srs;
    }

    public ValidationResultFlag validateDistance(UiCoordinateAttribute uiAttribute, double[] coordinate) {
        CoordinateAttribute attribute = (CoordinateAttribute) recordNodes.getAttribute(uiAttribute.getId());
        Coordinate previousValue = attribute.getValue();
        try {
            attribute.setValue(new Coordinate(coordinate[0], coordinate[1], getUiSpatialReferenceSystem(uiAttribute).id));
            ValidationResultFlag worstFlag = ValidationResultFlag.OK;
            for (Check<?> check : attribute.getDefinition().getChecks()) {
                if (check instanceof DistanceCheck) {
                    ValidationResultFlag flag = ((DistanceCheck) check).evaluate(attribute);
                    if (flag == ValidationResultFlag.ERROR)
                        return flag;
                    if (flag == ValidationResultFlag.WARNING)
                        worstFlag = flag;
                }
            }
            return worstFlag;
        } finally {
            attribute.setValue(previousValue);
        }

    }

    private SurveyFile getSurveyFileGuide() {
        if (selectedSurvey == null)
            return null;
        List<SurveyFile> surveyFiles = surveyManager.loadSurveyFileSummaries(selectedSurvey);
        return CollectionUtils.find(surveyFiles, new Predicate<SurveyFile>() {
            public boolean evaluate(SurveyFile item) {
                return item.getType() == SurveyFile.SurveyFileType.SURVEY_GUIDE;
            }
        });
    }

    public boolean hasSurveyGuide() {
        return selectedSurveyHasGuide;
    }

    public byte[] loadSurveyGuide() {
        SurveyFile surveyFileGuide = getSurveyFileGuide();
        if (surveyFileGuide == null) {
            return null;
        } else {
            return surveyManager.loadSurveyFileContent(surveyFileGuide);
        }
    }

    public CollectSurvey getSelectedSurvey() {
        return selectedSurvey;
    }

    public interface ExportListener {
        void beforeRecordExport(int recordId);
    }

}