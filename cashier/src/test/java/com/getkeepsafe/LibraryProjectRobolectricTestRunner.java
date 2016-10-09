package com.getkeepsafe;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;
import org.robolectric.res.FsFile;

import java.io.File;

/**
 * This exists solely for this issue: https://github.com/robolectric/robolectric/issues/2581
 */
public class LibraryProjectRobolectricTestRunner extends RobolectricTestRunner {
    public LibraryProjectRobolectricTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        AndroidManifest appManifest = super.getAppManifest(config);
        FsFile androidManifestFile = appManifest.getAndroidManifestFile();

        if (androidManifestFile.exists()) {
            return appManifest;
        } else {
            androidManifestFile = FileFsFile.from(config.buildDir(),
                    appManifest.getAndroidManifestFile().getPath()
                            .replace("build" + File.separatorChar, "")
                            .replace("full", "aapt"));
            return new AndroidManifest(androidManifestFile, appManifest.getResDirectory(), appManifest.getAssetsDirectory());
        }
    }
}
