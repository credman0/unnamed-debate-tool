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

package org.debatetool.gui;

import com.dlsc.preferencesfx.PreferencesFx;
import com.dlsc.preferencesfx.PreferencesFxEvent;
import com.dlsc.preferencesfx.model.Category;
import com.dlsc.preferencesfx.model.Group;
import com.dlsc.preferencesfx.model.Setting;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STHighlightColor;

import java.io.*;
import java.util.Properties;

public class SettingsHandler {
    private static StringProperty mongoIP = new SimpleStringProperty("");
    private static final String DEFAULT_MONGO_IP = "127.0.0.1";
    private static StringProperty mongoPort = new SimpleStringProperty("");
    private static final String DEFAULT_MONGO_PORT = "27017";
    private static ObjectProperty color = new SimpleObjectProperty("");
    private static BooleanProperty exportAnalytics = new SimpleBooleanProperty(true);
    private static Properties properties = new Properties();
    private static PreferencesFx preferencesFx;
    static{
        if (new File("config.properties").exists()) {
            try {
                InputStream in = new FileInputStream("config.properties");
                properties.load(in);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            properties = new Properties();
        }

        String portString = properties.getProperty("mongod_port");
        if (portString == null){
            // TODO standardize default properties somewhere
            mongoPort.setValue(DEFAULT_MONGO_PORT);
            properties.setProperty("mongod_port", DEFAULT_MONGO_PORT);
        }else{
            mongoPort.setValue(portString);
        }

        String ipString = properties.getProperty("mongod_ip");
        if (ipString == null){
            // TODO standardize default properties somewhere
            mongoIP.setValue(DEFAULT_MONGO_IP);
            properties.setProperty("mongod_ip", DEFAULT_MONGO_IP);
        }else{
            mongoIP.setValue(ipString);
        }

        String colorString = properties.getProperty("color");
        if(colorString==null){
            color.set(STHighlightColor.CYAN.toString());
            properties.setProperty("color", STHighlightColor.CYAN.toString());
        }else{
            color.setValue(colorString);
        }
        exportAnalytics.set(Boolean.parseBoolean(properties.getProperty("exportAnalytics", "true")));

        ObservableList<String> colorChoices = FXCollections.observableArrayList();
        colorChoices.add(STHighlightColor.CYAN.toString());
        colorChoices.add(STHighlightColor.YELLOW.toString());
        colorChoices.add(STHighlightColor.GREEN.toString());

        preferencesFx =
                PreferencesFx.of(SettingsHandler.class,
                        Category.of("Preferences",
                                Group.of("Display",
                                        Setting.of("Color", colorChoices, color)),
                                Group.of("Export",
                                        Setting.of("Export Analytics", exportAnalytics))
                        )
                ).addEventHandler(PreferencesFxEvent.EVENT_PREFERENCES_SAVED, new EventHandler<PreferencesFxEvent>() {
                    @Override
                    public void handle(PreferencesFxEvent preferencesFxEvent) {
                        try {
                            saveChanges();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
        /*

         */
    }

    public static String getColorTag(){
        String color = getSetting("color");
        if (color == null){
            color = "green";
        }
        if (color.equals("green")){
            return "#00e310";
        }else if (color.equals("cyan")){
            return "#91f5f5";
        }else if (color.equals("yellow")){
            return "#FFFF00";
        }else{
            throw new IllegalStateException("Unknown color: " + color);
        }
    }

    public static void showDialog(){
        preferencesFx.show();
    }

    public static String getSetting(String name){
        return properties.getProperty(name);
    }

    public static Object setSetting(String name, String value){
        return properties.setProperty(name,value);
    }

    public static void store() throws IOException {

        OutputStream out = new FileOutputStream("config.properties");
        properties.store(out, null);
        out.close();
    }

    private static void saveChanges() throws IOException {
        boolean changed = false;
        String colorProperty = properties.getProperty("color", STHighlightColor.CYAN.toString());
        if (colorProperty==null || !color.getValue().equals(colorProperty)) {
            properties.put("color", color.getValue());
            changed = true;
        }
        boolean exportAnalyticsProperty = Boolean.parseBoolean(properties.getProperty("exportAnalytics", "true"));
        if (exportAnalyticsProperty!=exportAnalytics.get()) {
            properties.put("exportAnalytics", exportAnalytics.getValue().toString());
            changed = true;
        }
        if (changed) {
            store();
        }
    }

    public static boolean getExportAnalytics() {
        return Boolean.parseBoolean(properties.getProperty("exportAnalytics", "true"));
    }
}
