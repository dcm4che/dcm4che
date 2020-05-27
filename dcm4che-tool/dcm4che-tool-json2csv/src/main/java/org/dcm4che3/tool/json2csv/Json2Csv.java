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

package org.dcm4che3.tool.json2csv;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Feb 2020
 */
public class Json2Csv {

    private static final String[] USAGE = {
            "usage: json2csv <schema-dir> <csv-dir>",
            "    or json2csv <schema-dir> <csv-dir> <out-schema-dir>",
            "",
            "The json2csv utility converts Archive configuration schema JSON files to CVS",
            "and vice versa to ease translation of attribute names and descriptions to",
            "other languages than English."
    };
    private static final String PROPERTY_TITLE_DESCRIPTION = "\"property\",\"title\",\"description\"";
    private final File schemaDir;
    private final File csvDir;

    public Json2Csv(File schemaDir, File csvDir) {
        this.schemaDir = schemaDir;
        this.csvDir = csvDir;
    }

    public static void main(String[] args) throws Exception {
        switch (args.length) {
            case 2:
                json2csv(new File(args[0]), new File(args[1]));
                break;
            case 3:
                csv2json(new File(args[0]), new File(args[1]), new File(args[2]));
                break;
            default:
                for (String line : USAGE) {
                    System.out.println(line);
                }
                System.exit(-1);
        }
    }

    public static void json2csv(File schemaDir, File csvDir) throws IOException {
        csvDir.mkdirs();
        for (String fname : schemaDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".schema.json");
            }
        })) {
            json2csv1(
                    new File(schemaDir, fname),
                    new File(csvDir, fname.substring(0, fname.length() - 4) + "csv"));
        }
    }

    private static void json2csv1(File inFile, File outFile) throws IOException {
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
                writer.write(PROPERTY_TITLE_DESCRIPTION);
                writer.write("\r\n\"\",\"");
                writer.write(doc.getString("title"));
                writer.write("\",\"");
                writer.write(doc.getString("description").replace("\"", "\"\""));
                for (String name : properties.keySet()) {
                    JsonObject property = properties.getJsonObject(name);
                    writer.write("\"\r\n\"");
                    writer.write(name);
                    writer.write("\",\"");
                    writer.write(property.getString("title"));
                    writer.write("\",\"");
                    writer.write(property.getString("description").replace("\"", "\"\""));
                }
                writer.write("\"\r\n");
            }
        }
    }

    public static void csv2json(File srcSchemaDir, File csvDir, File destSchemaDir) throws IOException {
        destSchemaDir.mkdirs();
        for (String fname : srcSchemaDir.list(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".schema.json");
            }
        })) {
            csv2json1(
                    new File(srcSchemaDir, fname),
                    new File(csvDir, fname.substring(0, fname.length() - 4) + "csv"),
                    new File(destSchemaDir, fname));
        }
    }

    private static void csv2json1(File schemaFile, File csvFile, File outFile) throws IOException {
        System.out.println(csvFile.toString() + "=>" + outFile.toString());
        Map<String, String[]> csv = parseCSV(csvFile);
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
                String[] csvLine = csv.get("\"\"");
                int field = csvLine != null ? 0 : 3;
                int fieldAfterDescription = 3;
                while ((line = reader.readLine()) != null) {
                    switch (field) {
                        case 0:
                            field = 1;
                            break;
                        case 1:
                            line = indent + "\"title\": " + csvLine[1] + ',';
                            field = 2;
                            break;
                        case 2:
                            line = indent + "\"description\": " + csvLine[2] + ',';
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
                                csvLine = csv.get(line.substring(4, line.length() - 3));
                                if (csvLine != null) {
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

    private static Map<String, String[]> parseCSV(File csvFile) throws IOException {
        Map<String, String[]> csv = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new FileInputStream(csvFile),
                StandardCharsets.UTF_8))) {
            String line;
            if (!PROPERTY_TITLE_DESCRIPTION.equals(line = reader.readLine())) {
                throw new IOException("Unexpected CSV header: " + line);
            }
            while ((line = reader.readLine()) != null) {
                String[] split = split(line);
                csv.put(split[0], split);
            }
        } catch (FileNotFoundException e) {}
        return csv;
    }

    private static String[] split(String line) {
        String[] fields = line.split(",");
        if (fields.length > 3) {
            StringBuilder sb = new StringBuilder();
            sb.append(fields[2]);
            for (int i = 3; i < fields.length; i++) {
                sb.append(',').append(fields[i]);
            }
            fields = new String[]{ fields[0], fields[1], sb.toString() };
        }
        fields[2] = fields[2].replace("\\", "\\\\").replace("\"\"", "\\\"");
        return fields;
    }

}
