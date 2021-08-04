/*
 * **** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is part of dcm4che, an implementation of DICOM(TM) in
 * Java(TM), hosted at https://github.com/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * J4Care.
 * Portions created by the Initial Developer are Copyright (C) 2015-2019
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * See @authors listed below
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * **** END LICENSE BLOCK *****
 *
 */

package org.dcm4che3.tool.json2props;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Feb 2020
 */
public class Json2Props {

    private static final String[] USAGE = {
            "usage: json2props <schema-dir> <props-dir>",
            "    or json2props <schema-dir> <props-dir> <out-schema-dir>",
            "",
            "The json2props utility converts Archive configuration schema JSON files",
            "to key-value properties files and vice versa to ease translation of",
            "attribute names and descriptions to other languages than English."
    };
    private final File schemaDir;
    private final File propsDir;

    public Json2Props(File schemaDir, File propsDir) {
        this.schemaDir = schemaDir;
        this.propsDir = propsDir;
    }

    public static void main(String[] args) throws Exception {
        switch (args.length) {
            case 2:
                json2props(new File(args[0]), new File(args[1]));
                break;
            case 3:
                props2json(new File(args[0]), new File(args[1]), new File(args[2]));
                break;
            default:
                for (String line : USAGE) {
                    System.out.println(line);
                }
                System.exit(-1);
        }
    }

    public static void json2props(File schemaDir, File propsDir) throws IOException {
        propsDir.mkdirs();
        for (String fname : schemaDir.list((dir, name) -> name.endsWith(".schema.json"))) {
            String prefix = fname.substring(0, fname.length() - 11);
            json2props1(
                    new File(schemaDir, fname),
                    new File(propsDir, prefix + "properties"),
                    prefix);
        }
    }

    private static void json2props1(File inFile, File outFile, String prefix) throws IOException {
        System.out.println(inFile.toString() + "=>" + outFile.toString());
        try (JsonReader reader = Json.createReader(
                new InputStreamReader(
                        new BufferedInputStream(new FileInputStream(inFile)),
                        StandardCharsets.UTF_8))) {
            try (Writer writer = new OutputStreamWriter(
                    new BufferedOutputStream(new FileOutputStream(outFile)),
                    StandardCharsets.UTF_8)) {
                JsonObject doc = reader.readObject();
                JsonObject properties = doc.getJsonObject("properties");
                writer.write(prefix.substring(0, prefix.length() - 1));
                writer.write(':');
                writer.write(doc.getString("title"));
                writer.write('|');
                writer.write(doc.getString("description").replace("\\", "\\\\"));
                writer.write('\r');
                writer.write('\n');
                for (String name : properties.keySet()) {
                    JsonObject property = properties.getJsonObject(name);
                    writer.write(prefix);
                    writer.write(name);
                    writer.write(':');
                    writer.write(property.getString("title"));
                    writer.write('|');
                    writer.write(property.getString("description").replace("\\", "\\\\"));
                    writer.write('\r');
                    writer.write('\n');
                }
            }
        }
    }

    public static void props2json(File srcSchemaDir, File propsDir, File destSchemaDir) throws IOException {
        destSchemaDir.mkdirs();
        for (String fname : srcSchemaDir.list((dir, name) -> name.endsWith(".schema.json"))) {
            String prefix = fname.substring(0, fname.length() - 11);
            props2json1(
                    new File(srcSchemaDir, fname),
                    new File(propsDir, prefix + "properties"),
                    new File(destSchemaDir, fname),
                    prefix);
        }
    }

    private static void props2json1(File schemaFile, File propsFile, File outFile, String prefix)
            throws IOException {
        System.out.println(propsFile.toString() + "=>" + outFile.toString());
        Properties props = new Properties();
        try (BufferedReader reader1 = new BufferedReader(new InputStreamReader(
                new FileInputStream(propsFile),
                StandardCharsets.UTF_8))) {
            props.load(reader1);
        } catch (FileNotFoundException e) {}
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    new FileInputStream(schemaFile),
                    StandardCharsets.UTF_8))) {
            try (BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(
                        new FileOutputStream(outFile),
                        StandardCharsets.UTF_8))) {
                String line;
                String indent = "  ";
                String value = props.getProperty(prefix.substring(0, prefix.length() - 1));
                int endTitle = -1;
                int field = value != null ? 0 : 3;
                int fieldAfterDescription = 3;
                while ((line = reader.readLine()) != null) {
                    switch (field) {
                        case 0:
                            field = 1;
                            break;
                        case 1:
                            endTitle = value.indexOf('|');
                            line = indent + "\"title\": \"" + value.substring(0, endTitle) + "\",";
                            field = 2;
                            break;
                        case 2:
                            line = indent + "\"description\": \"" + value.substring(endTitle + 1)
                                    .replace("\\", "\\\\")
                                    .replace("\"", "\\\"")
                                    + "\",";
                            field = fieldAfterDescription;
                            break;
                        case 3:
                            if (line.startsWith("  \"properties\"")) {
                                field = 4;
                                indent = "      ";
                                fieldAfterDescription = 4;
                            }
                            break;
                        case 4:
                            if (line.startsWith("    \"")) {
                                String key = prefix + line.substring(5, line.length() - 4);
                                value = props.getProperty(key);
                                if (value != null) {
                                    field = 1;
                                }
                            }
                    }
                    writer.write(line);
                    writer.write('\n');
                }
            }
        }
    }

}
