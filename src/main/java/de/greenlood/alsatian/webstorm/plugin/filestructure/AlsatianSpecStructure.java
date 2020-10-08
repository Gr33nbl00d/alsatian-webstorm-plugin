package de.greenlood.alsatian.webstorm.plugin.filestructure;

import com.intellij.lang.javascript.psi.JSFunction;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlsatianSpecStructure extends AbstractAlsatianElement {
    public AlsatianSpecStructure(@NotNull String name, JSFunction testMethod, @Nullable AlsatianSuiteStructure parent) {
        super(testMethod, name, parent);
    }

}
