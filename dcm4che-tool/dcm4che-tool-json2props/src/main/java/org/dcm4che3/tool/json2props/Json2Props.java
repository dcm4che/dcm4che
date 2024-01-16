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

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.tool.common.CLIUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;

/**
 * @author Gunter Zeilinger (gunterze@protonmail.com)
 * @since Feb 2020
 */
public class Json2Props {

    private static final ResourceBundle rb = ResourceBundle.getBundle("org.dcm4che3.tool.json2props.messages");
    private final File schemaDir;
    private final File propsDir;

    public Json2Props(File schemaDir, File propsDir) {
        this.schemaDir = schemaDir;
        this.propsDir = propsDir;
    }

    private static CommandLine parseCommandLine(String[] args)
            throws ParseException {
        Options opts = new Options();
        CLIUtils.addCommonOptions(opts);
        return CLIUtils.parseComandLine(args, opts, rb, Json2Props.class);
    }

    public static void main(String[] args) {
        try {
            CommandLine cl = parseCommandLine(args);
            List<String> argsList = cl.getArgList();
            switch (argsList.size()) {
                case 0:
                case 1:
                    throw new ParseException(rb.getString("missing-args"));
                case 2:
                    json2props(new File(argsList.get(0)), new File(argsList.get(1)));
                    break;
                case 3:
                    props2json(new File(argsList.get(0)), new File(argsList.get(1)), new File(argsList.get(2)));
                    break;
                default:
                    throw new ParseException(rb.getString("to-many-args"));
            }
        } catch (ParseException e) {
            System.err.println("json2props: " + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        } catch (Exception e) {
            System.err.println("json2props: " + e.getMessage());
            e.printStackTrace();
            System.exit(2);
        }
    }

    public static void json2props(File schemaDir, File propsDir) throws IOException {
        String[] fnames = schemaDir.list((dir, name) -> name.endsWith(".schema.json"));
        if (fnames == null || fnames.length == 0) {
            System.err.println("No schema files found in " + schemaDir);
            System.err.println(rb.getString("try"));
            return;
        }
        propsDir.mkdirs();
        for (String fname : fnames) {
            String prefix = fname.substring(0, fname.length() - 11);
            json2props1(
                    new File(schemaDir, fname),
                    new File(propsDir, prefix + "props"),
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
        String[] fnames = srcSchemaDir.list((dir, name) -> name.endsWith(".schema.json"));
        if (fnames == null || fnames.length == 0) {
            System.err.println("No schema files found in " + srcSchemaDir);
            System.err.println(rb.getString("try"));
            return;
        }
        destSchemaDir.mkdirs();
        for (String fname : fnames) {
            String prefix = fname.substring(0, fname.length() - 11);
            props2json1(
                    new File(srcSchemaDir, fname),
                    new File(propsDir, prefix + "props"),
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
        List<String> invalidProperties = new ArrayList<>();
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
                int endTitle = -1;
                int field = 3;
                int fieldAfterDescription = 3;
                String key = prefix.substring(0, prefix.length() - 1);
                String value = props.getProperty(key);
                if (value != null) {
                    endTitle = value.indexOf('|');
                    if (endTitle > 0) {
                        field = 0;
                    } else {
                        invalidProperties.add(key);
                    }
                }
                while ((line = reader.readLine()) != null) {
                    switch (field) {
                        case 0:
                            field = 1;
                            break;
                        case 1:
                            line = indent + "\"title\": \"" + value.substring(0, endTitle).trim() + "\",";
                            field = 2;
                            break;
                        case 2:
                            line = indent + "\"description\": \"" + value.substring(endTitle + 1).trim()
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
                                key = prefix + line.substring(5, line.length() - 4);
                                value = props.getProperty(key);
                                if (value != null) {
                                    endTitle = value.indexOf('|');
                                    if (endTitle > 0) {
                                        field = 1;
                                    } else {
                                        invalidProperties.add(key);
                                    }
                                }
                            }
                    }
                    writer.write(line);
                    writer.write('\n');
                }
            }
        }
        if (!invalidProperties.isEmpty()) {
            System.out.printf("IGNORED %d PROPERTIES WITH MISSING |:%n", invalidProperties.size());
            invalidProperties.forEach(key -> System.out.printf("%s:%s%n", key, props.get(key)));
        }
    }

}
