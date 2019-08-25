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

import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.fxn.stash.Stash;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    private List<String> aggressiveFilters;
    private List<String> genericFilters;
    private List<String> allFilters;
    private FileScanner fs;

    @Before
    public void init() {
        Context appContext = InstrumentationRegistry.getTargetContext();
        Resources res = appContext.getResources();
        File path = new File(Environment.getExternalStorageDirectory().toString() + "/");
        fs = new FileScanner(path);
        Stash.init(appContext);
        fs.setAutoWhite(false);
        fs.setDelete(true);

        allFilters = new ArrayList<>();
        genericFilters = Arrays.asList(res.getStringArray(R.array.generic_filter_array));
        aggressiveFilters = Arrays.asList(res.getStringArray(R.array.aggressive_filter_array));
        allFilters.addAll(aggressiveFilters);
        allFilters.addAll(genericFilters);
    }

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("theredspy15.ltecleanerfoss", appContext.getPackageName());
    }

    @Test
    public void checkLogFiles() {
        File logFile = createFile("testfile.log");
        File clogFile = createFile("clogs.png");
        fs.setUpFilters(genericFilters, false);
        fs.startScan();

        assertTrue(clogFile.exists());
        assertFalse(logFile.exists());
    }

    @Test
    public void checkTempFiles() {
        File tmpFile = createFile("testfile.tmp");
        fs.setUpFilters(genericFilters, false);
        fs.startScan();

        assertFalse(tmpFile.exists());
    }

    @Test
    public void checkThumbFiles() {
        File thumbFile = createFile("thumbs.db");
        fs.setUpFilters(aggressiveFilters, false);
        fs.startScan();

        assertFalse(thumbFile.exists());
    }

    @Test
    public void checkAPKFiles() {
        File thumbFile = createFile("chrome.apk");
        fs.setUpFilters(allFilters, true);
        fs.startScan();

        assertFalse(thumbFile.exists());
    }

    @Test
    public void checkEmptyDir() {
        File emptyDir = createDir("testdir");
        fs.setUpFilters(genericFilters, false);
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
        file.mkdir();

        assertTrue(file.exists());
        return file;
    }
}
