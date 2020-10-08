package de.greenlood.alsatian.webstorm.plugin.filestructure;

import com.intellij.javascript.testFramework.AbstractTestFileStructureBuilder;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecma6.ES6Decorator;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;

public class AlsatianFileStructureBuilder extends AbstractTestFileStructureBuilder<AlsatianFileStructure> {
    private static final AlsatianFileStructureBuilder INSTANCE = new AlsatianFileStructureBuilder();

    public AlsatianFileStructureBuilder() {
    }

    @NotNull
    public AlsatianFileStructure buildTestFileStructure(@NotNull JSFile jsFile) {
        return new Builder(jsFile).build();
    }

    public static AlsatianFileStructureBuilder getInstance() {
        return INSTANCE;
    }

    private static class Builder {
        private final AlsatianFileStructure myFileStructure;

        Builder(JSFile jsFile) {
            this.myFileStructure = new AlsatianFileStructure(jsFile);
        }

        public AlsatianFileStructure build() {
            if (isAlsatianTestFile(myFileStructure.getJsFile())) {
                AlsatianFileStructure alsatianFileStructure = new AlsatianFileStructure(myFileStructure.getJsFile());
                AlsatianSuiteStructure suiteStructure = null;
                JSSourceElement[] statements = myFileStructure.getJsFile().getStatements();
                for (JSSourceElement statement : statements) {
                    if ("TypeScript".equals(statement.getLanguage().getID())) {
                        if (statement instanceof TypeScriptClass) {
                            TypeScriptClass typeScriptClass = (TypeScriptClass) statement;
                            @NotNull ES6Decorator[] classDecorators = typeScriptClass.getAttributeList().getDecorators();
                            for (ES6Decorator classDecorator : classDecorators) {
                                if ("TestFixture".equals(classDecorator.getDecoratorName())) {
                                    String fixtureClassName = typeScriptClass.getName();
                                    String fixtureName = null;
                                    PsiElement[] children = classDecorator.getExpression().getChildren();
                                    @NotNull JSExpression[] expressions = ((JSArgumentList) children[1]).getArguments();
                                    for (JSExpression jsExpression : expressions) {
                                        if (jsExpression instanceof JSLiteralExpression) {
                                            fixtureName = ((JSLiteralExpression) jsExpression).getStringValue();
                                        }
                                    }

                                    suiteStructure = new AlsatianSuiteStructure(typeScriptClass, fixtureClassName, fixtureName, statement.getContainingFile().getVirtualFile().getPath(), null);
                                    alsatianFileStructure.addSuiteStructure(suiteStructure);

                                }
                            }
                            if (suiteStructure != null) {
                                @NotNull JSFunction[] functions = typeScriptClass.getFunctions();
                                for (JSFunction function : functions) {
                                    @NotNull ES6Decorator[] decorators = function.getAttributeList().getDecorators();
                                    for (ES6Decorator decorator : decorators) {
                                        if ("Test".equals(decorator.getDecoratorName())) {
                                            suiteStructure.addChild(new AlsatianSpecStructure(function.getName(), function, suiteStructure));
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
                return alsatianFileStructure;
            }

            return this.myFileStructure;
        }

    }

    public static boolean isAlsatianTestFile(JSFile jsFile) {
        JSSourceElement[] statements = jsFile.getStatements();
        for (JSSourceElement statement : statements) {
            if ("TypeScript".equals(statement.getLanguage().getID())) {
                if (statement instanceof TypeScriptClass) {
                    TypeScriptClass typeScriptClass = (TypeScriptClass) statement;
                    @NotNull ES6Decorator[] classDecorators = typeScriptClass.getAttributeList().getDecorators();
                    for (ES6Decorator classDecorator : classDecorators) {
                        if ("TestFixture".equals(classDecorator.getDecoratorName())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
