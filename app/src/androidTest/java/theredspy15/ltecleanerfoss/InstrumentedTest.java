/*
 *  Copyright 2018 TheRedSpy15
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */

package theredspy15.ltecleanerfoss;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.fxn.stash.Stash;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class InstrumentedTest {
    private FileScanner fs;

    @Before
    public void init() {
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        File path = new File(Environment.getExternalStorageDirectory().toString() + "/");
        Resources res = appContext.getResources();
        fs = new FileScanner(path);
        Stash.init(appContext);
        fs.setAutoWhite(false);
        fs.setResources(res);
        fs.setDelete(true);
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        assertEquals("theredspy15.ltecleanerfoss", appContext.getPackageName());
    }

    @Test
    public void checkLogFiles() {
        File logFile = createFile("testfile.loG");
        File clogFile = createFile("clogs.pnG");
        fs.setUpFilters(true,
                false, false);
        fs.startScan();

        assertTrue(clogFile.exists());
        assertFalse(logFile.exists());
    }

    @Test
    public void checkTempFiles() {
        File tmpFile = createFile("testfile.tMp");
        fs.setUpFilters(true,
                false, false);
        fs.startScan();

        assertFalse(tmpFile.exists());
    }

    @Test
    public void checkThumbFiles() {
        File thumbFile = createFile("thumbs.Db");
        fs.setUpFilters(false,
                true, false);
        fs.startScan();

        assertFalse(thumbFile.exists());
    }

    @Test
    public void checkAPKFiles() {
        File thumbFile = createFile("chrome.aPk");
        fs.setUpFilters(true,
                true, true);
        fs.startScan();

        assertFalse(thumbFile.exists());
    }

    @Test
    public void checkEmptyDir() {
        File emptyDir = createDir("testdir");
        fs.setUpFilters(true,
                false, false);
        fs.setEmptyDir(true);
        fs.startScan();

        assertFalse(emptyDir.exists());
    }

    private File createFile(String name) {
        File file = new File(Environment.getExternalStorageDirectory().toString()
                + "/" + name);
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        assertTrue(file.exists());
        return file;
    }

    private File createDir(String name) {
        File file = new File(Environment.getExternalStorageDirectory(), name);

        assertTrue(file.mkdir());
        assertTrue(file.exists());
        return file;
    }
}
