package de.greenlood.alsatian.webstorm.plugin.runconfig;

import com.google.gson.Gson;
import com.intellij.javascript.nodejs.interpreter.NodeJsInterpreterRef;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import com.intellij.openapi.util.text.StringUtil;
import de.greenlood.alsatian.webstorm.plugin.executionplan.AlsatianExecutionPlan;
import org.jdom.Element;

import java.util.List;

public class AlsatianRunConfigurationPersister {
    private static final Gson gson = new Gson();

    public void write(Element element, Project project, AlsatianRunSettings runSettings) {
        JDOMExternalizerUtil.writeCustomField(element, "scope-kind", runSettings.getScopeKind().toString());
        JDOMExternalizerUtil.writeCustomField(element, "working-dir", runSettings.getWorkingDir());
        String executionPlanJson = gson.toJson(runSettings.getAlsatianExecutionPlan());
        JDOMExternalizerUtil.writeCustomField(element, "execution-plan", executionPlanJson);
        runSettings.getEnvData().writeExternal(element);
        JDOMExternalizerUtil.writeCustomField(element, "node-options", runSettings.getNodeOptions());
        JDOMExternalizerUtil.writeCustomField(element, "node-interpreter", runSettings.getInterpreterRef().getReferenceName());
        String listOfGlobs = gson.toJson(runSettings.getListOfGlobs());
        JDOMExternalizerUtil.writeCustomField(element, "list-globs", listOfGlobs);
    }

    public AlsatianRunSettings read(Element element, Project project) {

        AlsatianRunSettings.Builder builder = new AlsatianRunSettings.Builder();
        String interpreterRefName = JDOMExternalizerUtil.readCustomField(element, "node-interpreter");
        builder.setInterpreterRef(NodeJsInterpreterRef.create(interpreterRefName));
        builder.setNodeOptions(StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, "node-options")));
        builder.setScopeKind(AlsatianScopeKind.valueOf(StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, "scope-kind"))));
        builder.setWorkingDir(StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, "working-dir")));
        String listGlobsString = StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, "list-globs"));
        List<String> listGlobs = gson.fromJson(listGlobsString, List.class);
        builder.setListOfGlobs(listGlobs);
        String executionPlanString = StringUtil.notNullize(JDOMExternalizerUtil.readCustomField(element, "execution-plan"));
        AlsatianExecutionPlan alsatianExecutionPlan = gson.fromJson(executionPlanString, AlsatianExecutionPlan.class);
        builder.setAlsatianExecutionPlan(alsatianExecutionPlan);
        return builder.build();
    }
}
