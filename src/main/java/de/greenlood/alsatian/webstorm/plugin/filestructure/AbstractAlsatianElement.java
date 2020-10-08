package de.greenlood.alsatian.webstorm.plugin.filestructure;

import com.intellij.javascript.testFramework.AbstractTestStructureElement;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractAlsatianElement extends AbstractTestStructureElement<AlsatianSuiteStructure> {
    public AbstractAlsatianElement(@Nullable PsiElement element, @NotNull String name, @Nullable AlsatianSuiteStructure parent) {
        super(element, name, parent);
    }


}



