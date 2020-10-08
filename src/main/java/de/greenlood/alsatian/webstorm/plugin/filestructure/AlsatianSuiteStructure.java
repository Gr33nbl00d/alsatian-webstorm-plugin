package de.greenlood.alsatian.webstorm.plugin.filestructure;

import com.intellij.javascript.testFramework.AbstractTestStructureElement;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AlsatianSuiteStructure extends AbstractAlsatianElement {

    private final List<AbstractAlsatianElement> myChildren;
    private final String fixtureName;
    private final String filePath;
    private final AlsatianSuiteStructure parent;

    public AlsatianSuiteStructure(TypeScriptClass typeScriptClass, String fixtureClassName, String fixtureName, @NotNull String filePath, @Nullable AlsatianSuiteStructure parent) {
        super(typeScriptClass, fixtureClassName, null);
        this.fixtureName = fixtureName;
        this.filePath = filePath;
        this.parent = parent;
        this.myChildren = new ArrayList();
    }

    public void addChild(@NotNull AbstractAlsatianElement child) {
        this.myChildren.add(child);
    }

    @Nullable
    public AlsatianSpecStructure getInnerSpecByName(String specName) {
        for (AbstractAlsatianElement mySpecChild : this.myChildren) {
            AlsatianSpecStructure innerSpecByName = getInnerSpecByName(specName, mySpecChild);
            if(innerSpecByName != null)
                return innerSpecByName;
        }
        return null;
    }

    private AlsatianSpecStructure getInnerSpecByName(String specName, AbstractAlsatianElement child) {
        if(child instanceof AlsatianSpecStructure)
        {
             AlsatianSpecStructure mySpecChild = (AlsatianSpecStructure) child;
            if(mySpecChild.getName().equals(specName))
                return mySpecChild;
            else
                return null;
        }
        else{
            return getInnerSpecByName(specName,child);
        }
    }

    @Override
    public @NotNull List<? extends AbstractTestStructureElement> getChildren() {
        return myChildren;
    }

    public String getFixtureName() {
        return fixtureName;
    }

    public String getFilePath() {
        return filePath;
    }

}
