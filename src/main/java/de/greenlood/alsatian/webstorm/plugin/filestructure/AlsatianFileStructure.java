package de.greenlood.alsatian.webstorm.plugin.filestructure;

import com.google.common.collect.Lists;
import com.intellij.javascript.testFramework.AbstractTestFileStructure;
import com.intellij.javascript.testFramework.AbstractTestStructureElement;
import com.intellij.javascript.testFramework.JsTestElementPath;
import com.intellij.javascript.testFramework.JstdRunElement;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.util.SmartList;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AlsatianFileStructure extends AbstractTestFileStructure {
    private final List<AlsatianSuiteStructure> mySuiteStructures;
    private final List<AbstractAlsatianElement> myChildren;
    private final Map<PsiElement, AbstractAlsatianElement> myLeafElementToInfoMap;

    public AlsatianFileStructure(@NotNull JSFile jsFile) {
        super(jsFile);
        this.mySuiteStructures = Lists.newArrayList();
        this.myChildren = Lists.newArrayList();
        this.myLeafElementToInfoMap = ContainerUtil.newIdentityHashMap();
    }

    @Override
    public boolean isEmpty() {
        return myLeafElementToInfoMap.isEmpty();
    }

    @NotNull
    public List<AlsatianSuiteStructure> getSuites() {
        return this.mySuiteStructures;
    }

    public void addSuiteStructure(@NotNull AlsatianSuiteStructure suiteStructure) {
        this.mySuiteStructures.add(suiteStructure);
        this.myChildren.add(suiteStructure);
    }

    @NotNull
    public List<AbstractAlsatianElement> getChildren() {
        return this.myChildren;
    }

    @Nullable
    public JstdRunElement findJstdRunElement(@NotNull TextRange textRange) {
        JsTestElementPath testElementPath = this.findTestElementPath(textRange);
        return testElementPath == null ? null : new JstdRunElement(StringUtil.join(testElementPath.getSuiteNames(), " "), testElementPath.getTestName());
    }

    @Nullable
    public AbstractAlsatianElement findAlsatianElement(@NotNull TextRange textRange) {

        AlsatianSuiteStructure suiteStructureCandidate = null;
        for (AlsatianSuiteStructure mySuiteStructure : mySuiteStructures) {
            TextRange textRange1 = mySuiteStructure.getEnclosingPsiElement().getTextRange();
            if (textRange1.contains(textRange)) {
                suiteStructureCandidate = mySuiteStructure;
            }
            for (AbstractTestStructureElement child : mySuiteStructure.getChildren()) {
                TextRange textRange2 = child.getEnclosingPsiElement().getTextRange();
                if (textRange2.contains(textRange))
                    return (AbstractAlsatianElement) child;
            }
        }
        return suiteStructureCandidate;
    }

    @Nullable
    public JsTestElementPath findTestElementPath(@NotNull TextRange textRange) {
        return toTestElementPath(this.findAlsatianElement(textRange));
    }

    @Nullable
    public JsTestElementPath findTestElementPath(@NotNull PsiElement testIdentifierLeafElement) {
        return toTestElementPath(this.myLeafElementToInfoMap.get(testIdentifierLeafElement));
    }


    @Nullable
    private static JsTestElementPath toTestElementPath(@Nullable AbstractAlsatianElement element) {
        if (element == null) {
            return null;
        } else {
            String specName = null;
            PsiElement psiElement;
            AlsatianSuiteStructure suiteStructure;
            if (element instanceof AlsatianSuiteStructure) {
                suiteStructure = (AlsatianSuiteStructure) element;
                psiElement = suiteStructure.getEnclosingPsiElement();
            } else {
                AlsatianSpecStructure specStructure = (AlsatianSpecStructure) element;
                suiteStructure = element.getParent();
                specName = element.getName();
                psiElement = specStructure.getEnclosingPsiElement();
            }

            List suites;
            for (suites = new SmartList(); suiteStructure != null; suiteStructure = suiteStructure.getParent()) {
                suites.add(suiteStructure.getName());
            }

            Collections.reverse(suites);
            return new JsTestElementPath(suites, specName, psiElement);
        }
    }

    public PsiElement findPsiElement(@NotNull String filePath, @Nullable String metaInfo) {
        String[] metaInfoSplit = metaInfo.split(":");
        if (isTestFixtureMetaInfo(metaInfoSplit)) {
            return getSuiteByMetaInfo(metaInfoSplit[0]);
        }
        if (isTestMethodMetaInfo(metaInfoSplit)) {
            return getTestMethodByMetaInfo(metaInfoSplit);
        } else {
            throw new IllegalStateException("meta info is invalid");
        }

    }

    @Nullable
    private PsiElement getTestMethodByMetaInfo(String[] metaInfoSplit) {
        AlsatianSuiteStructure suite = this.findSuiteByName(metaInfoSplit[0]);
        AlsatianSpecStructure spec = suite.getInnerSpecByName(metaInfoSplit[1]);
        if (spec != null)
            return spec.getEnclosingPsiElement();
        else
            return null;
    }

    @Nullable
    private PsiElement getSuiteByMetaInfo(String name) {
        AlsatianSuiteStructure suite = this.findSuiteByName(name);
        if (suite == null)
            return null;
        else
            return suite.getEnclosingPsiElement();
    }

    private boolean isTestMethodMetaInfo(String[] metaInfoSplit) {
        return metaInfoSplit.length == 2;
    }

    private boolean isTestFixtureMetaInfo(String[] metaInfoSplit) {
        return metaInfoSplit.length == 1;
    }

    @Nullable
    public AlsatianSuiteStructure findSuiteByName(@NotNull String name) {
        for (AlsatianSuiteStructure suite : this.getSuites()) {
            if (suite.getName().equals(name))
                return suite;
        }
        return null;
    }


    @NotNull
    public List<String> getTopLevelElements() {
        int size = this.mySuiteStructures.size();
        if (size == 0) {
            return Collections.emptyList();
        } else {
            List<String> out = new ArrayList(size);
            Iterator suiteIterator = this.mySuiteStructures.iterator();

            while (suiteIterator.hasNext()) {
                AlsatianSuiteStructure suite = (AlsatianSuiteStructure) suiteIterator.next();
                out.add(suite.getName());
            }
            return out;
        }
    }

    @NotNull
    public List<String> getChildrenOf(@NotNull String topLevelElementName) {
        return Collections.emptyList();
    }

    public boolean contains(@NotNull String testCaseName, @Nullable String testMethodName) {
        return this.findPsiElement(testCaseName, testMethodName) != null;
    }

}
