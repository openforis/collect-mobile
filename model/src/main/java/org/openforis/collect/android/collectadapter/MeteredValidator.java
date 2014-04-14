package org.openforis.collect.android.collectadapter;

import org.openforis.collect.android.gui.util.meter.Timer;
import org.openforis.collect.manager.CodeListManager;
import org.openforis.collect.model.validation.CollectValidator;
import org.openforis.idm.metamodel.NodeDefinition;
import org.openforis.idm.metamodel.validation.*;
import org.openforis.idm.model.*;

import java.util.concurrent.Callable;

/**
 * @author Daniel Wiell
 */
public class MeteredValidator extends CollectValidator {
    public MeteredValidator(CodeListManager codeListManager) {
        setCodeListManager(codeListManager);
    }

    public ValidationResults validate(final Attribute<?, ?> attribute) {
        return time("validate", new Callable<ValidationResults>() {
            public ValidationResults call() throws Exception {
                return MeteredValidator.super.validate(attribute);
            }
        });
    }

    public ValidationResultFlag validateMinCount(final Entity entity, final String childName) {
        return time("validateMinCount", new Callable<ValidationResultFlag>() {
            public ValidationResultFlag call() throws Exception {
                return MeteredValidator.super.validateMinCount(entity, childName);
            }
        });
    }

    public ValidationResultFlag validateMaxCount(final Entity entity, final String childName) {
        return time("validateMaxCount", new Callable<ValidationResultFlag>() {
            public ValidationResultFlag call() throws Exception {
                return MeteredValidator.super.validateMaxCount(entity, childName);
            }
        });
    }

    protected MinCountValidator getMinCountValidator(final NodeDefinition defn) {
        return time("getMinCountValidator", new Callable<MinCountValidator>() {
            public MinCountValidator call() throws Exception {
                return MeteredValidator.super.getMinCountValidator(defn);
            }
        });
    }

    protected MaxCountValidator getMaxCountValidator(final NodeDefinition defn) {
        return time("getMaxCountValidator", new Callable<MaxCountValidator>() {
            public MaxCountValidator call() throws Exception {
                return MeteredValidator.super.getMaxCountValidator(defn);
            }
        });
    }

    protected TaxonVernacularLanguageValidator getTaxonVernacularLanguageValidator() {
        return time("getTaxonVernacularLanguageValidator", new Callable<TaxonVernacularLanguageValidator>() {
            public TaxonVernacularLanguageValidator call() throws Exception {
                return MeteredValidator.super.getTaxonVernacularLanguageValidator();
            }
        });
    }

    protected CodeValidator getCodeValidator() {
        return time("getCodeValidator", new Callable<CodeValidator>() {
            public CodeValidator call() throws Exception {
                return MeteredValidator.super.getCodeValidator();
            }
        });
    }

    protected CodeParentValidator getCodeParentValidator() {
        return time("getCodeParentValidator", new Callable<CodeParentValidator>() {
            public CodeParentValidator call() throws Exception {
                return MeteredValidator.super.getCodeParentValidator();
            }
        });
    }

    protected void validateAttributeChecks(final Attribute<?, ?> attribute, final ValidationResults results) {
        time("validateAttributeChecks", new Runnable() {
            public void run() {
                MeteredValidator.super.validateAttributeChecks(attribute, results);
            }
        });
    }

    protected void validateAttributeValue(final Attribute<?, ?> attribute, final ValidationResults results) {
        time("validateAttributeValue", new Runnable() {
            public void run() {
                MeteredValidator.super.validateAttributeValue(attribute, results);
            }
        });
    }

    protected void validateNumericAttributeValue(final NumberAttribute<?, ?> attribute, final ValidationResults results) {
        time("validateNumericAttributeValue", new Runnable() {
            public void run() {
                MeteredValidator.super.validateNumericAttributeValue(attribute, results);
            }
        });
    }

    protected void validateNumericAttributeUnit(final NumberAttribute<?, ?> attribute, final ValidationResults results) {
        time("validateNumericAttributeUnit", new Runnable() {
            public void run() {
                MeteredValidator.super.validateNumericAttributeUnit(attribute, results);
            }
        });
    }

    protected void validateIntegerRangeAttributeValue(final IntegerRangeAttribute attribute, final ValidationResults results) {
        time("validateIntegerRangeAttributeValue", new Runnable() {
            public void run() {
                MeteredValidator.super.validateIntegerRangeAttributeValue(attribute, results);
            }
        });
    }

    protected void validateRealRangeAttributeValue(final RealRangeAttribute attribute, final ValidationResults results) {
        time("validateRealRangeAttributeValue", new Runnable() {
            public void run() {
                MeteredValidator.super.validateRealRangeAttributeValue(attribute, results);
            }
        });
    }

    protected void validateNumericRangeUnit(final NumericRangeAttribute<?, ?> attribute, final ValidationResults results) {
        time("validateNumericRangeUnit", new Runnable() {
            public void run() {
                MeteredValidator.super.validateNumericRangeUnit(attribute, results);
            }
        });
    }

    protected void validateTaxonAttributeValue(final TaxonAttribute attribute, final ValidationResults results) {
        time("validateTaxonAttributeValue", new Runnable() {
            public void run() {
                MeteredValidator.super.validateTaxonAttributeValue(attribute, results);
            }
        });
    }

    private <T> T time(String methodName, Callable<T> action) {
        return Timer.time(CollectValidator.class, methodName, action);
    }

    private void time(String methodName, Runnable action) {
        Timer.time(CollectValidator.class, methodName, action);
    }
}
