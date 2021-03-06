/*
 *                               This program is free software: you can redistribute it and/or modify
 *                                it under the terms of the GNU General Public License as published by
 *                                the Free Software Foundation, version 3 of the License.
 *
 *                                This program is distributed in the hope that it will be useful,
 *                                but WITHOUT ANY WARRANTY; without even the implied warranty of
 *                                MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *                                GNU General Public License for more details.
 *
 *                                You should have received a copy of the GNU General Public License
 *                                along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *                                Copyright (c) 2019 Colin Redman
 */

package io.filesystem;

import org.debatetool.core.Analytic;
import org.debatetool.core.Block;
import org.debatetool.core.Card;
import org.debatetool.core.Cite;
import org.debatetool.io.IOUtil;
import org.debatetool.io.componentio.ComponentIOManager;
import org.debatetool.io.filesystemio.FileSystemIOController;
import org.debatetool.io.initializers.FileSystemInitializer;
import org.debatetool.io.iocontrollers.IOController;
import org.debatetool.io.iocontrollers.mongodb.MongoDBIOController;
import org.junit.AfterClass;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;

class FileSystemBlockIOManagerTest {
    Block block;
    public final String ANALYTIC_TEXT = "This is an analytic";
    public final Path DIR_PATH = Paths.get("test_fs_data_dir");

    @BeforeClass
    public void setUp() throws IOException {
        if (DIR_PATH.toFile().exists()){
            IOUtil.deleteDir(DIR_PATH.toFile());
        }
        IOController.setIoController(new FileSystemIOController());
        IOController.getIoController().attemptInitialize(new FileSystemInitializer(DIR_PATH));
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource("testCardText.txt").getFile());
        String text = null;
        try (BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))){
            text = new String(in.readAllBytes());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Card card1 = new Card(new Cite("Smith", "2010", "Renowned writer of cards"), text);
        Card card2 = new Card(new Cite("Smith", "2010", "Renowned writer of cards"), text+"AAA");
        block = new Block("Test Block");
        block.addComponent(card1);
        block.addComponent(card2);
        block.addComponent(new Analytic(ANALYTIC_TEXT));
        try {
            IOController.getIoController().getComponentIOManager().storeSpeechComponent(card1);
            IOController.getIoController().getComponentIOManager().storeSpeechComponent(card2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void writeReadTest () {
        try {
            ComponentIOManager manager = IOController.getIoController().getComponentIOManager();
            manager.storeSpeechComponent(block);
            Block recovered = (Block) manager.retrieveSpeechComponent(block.getHash());
            recovered.load();
            Assert.assertEquals(recovered,block);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @AfterClass
    public void tearDown(){
        try {
            IOUtil.deleteDir(DIR_PATH.toFile());
            IOController.getIoController().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}