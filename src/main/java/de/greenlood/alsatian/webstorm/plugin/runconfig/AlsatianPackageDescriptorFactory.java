package de.greenlood.alsatian.webstorm.plugin.runconfig;

import com.intellij.javascript.nodejs.util.NodePackageDescriptor;

public class AlsatianPackageDescriptorFactory {
    public NodePackageDescriptor getPackageDescriptor() {
        return new NodePackageDescriptor("alsatian");
    }
}
