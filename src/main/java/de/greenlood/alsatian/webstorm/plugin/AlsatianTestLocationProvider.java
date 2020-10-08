package de.greenlood.alsatian.webstorm.plugin;

import com.intellij.execution.Location;
import com.intellij.execution.PsiLocation;
import com.intellij.execution.testframework.sm.FileUrlProvider;
import com.intellij.execution.testframework.sm.TestsLocationProviderUtil;
import com.intellij.execution.testframework.sm.runner.SMTestLocator;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.containers.ContainerUtil;
import de.greenlood.alsatian.webstorm.plugin.filestructure.AlsatianFileStructure;
import de.greenlood.alsatian.webstorm.plugin.filestructure.AlsatianFileStructureBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class AlsatianTestLocationProvider implements SMTestLocator {

    public AlsatianTestLocationProvider() {
    }

    public List<Location> getLocation(@NotNull String protocol, @NotNull String path, @Nullable String metaInfo, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        if ("file".equals(protocol)) {

            return FileUrlProvider.INSTANCE.getLocation(protocol, path, project, scope);
        } else {
            Location location;
            if (!"suite".equals(protocol) && !"test".equals(protocol)) {
                location = null;
            } else {
                location = this.getTestLocation(project, path, metaInfo);
            }

            if (location != null) {

                return Collections.singletonList(location);
            } else {
                return Collections.emptyList();
            }
        }
    }

    @NotNull
    public List<Location> getLocation(@NotNull String protocol, @NotNull String path, @NotNull Project project, @NotNull GlobalSearchScope scope) {
        //this method is not used as only the method with meta info is used
        return Collections.emptyList();
    }

    @Nullable
    private Location getTestLocation(@NotNull Project project, @NotNull String locationData, @Nullable String metainfo) {
        if (locationData == null) {
            return null;
        } else {
            List<VirtualFile> testFiles = TestsLocationProviderUtil.findSuitableFilesFor(locationData, project);
            VirtualFile testFile = ContainerUtil.getFirstItem(testFiles);
            PsiFile file = PsiManager.getInstance(project).findFile(testFile);
            AlsatianFileStructureBuilder builder = AlsatianFileStructureBuilder.getInstance();
            if (file instanceof JSFile) {
                AlsatianFileStructure alsatianFileStructure = builder.fetchCachedTestFileStructure((JSFile) file);
                PsiElement element = alsatianFileStructure.findPsiElement(locationData, metainfo);
                if (element != null && element.isValid()) {
                    return element != null ? PsiLocation.fromPsiElement(element) : null;
                }
            }
        }
        return null;
    }

}
