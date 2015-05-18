/* ***** BEGIN LICENSE BLOCK *****
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
 * Java(TM), hosted at https://github.com/gunterze/dcm4che.
 *
 * The Initial Developer of the Original Code is
 * Agfa Healthcare.
 * Portions created by the Initial Developer are Copyright (C) 2011
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
 * ***** END LICENSE BLOCK ***** */
package org.dcm4che3.tool.dcmdict;


import java.lang.reflect.Field;

import org.dcm4che3.data.UID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.dcm4che3.data.ElementDictionary;
import org.dcm4che3.tool.common.CLIUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Hesham Elbadawi <bsdreko@gmail.com>
 * 
 */

public class DcmDict{
    
    private static final Logger LOG = LoggerFactory.getLogger(DcmDict.class);
    
    private static Options options;
    
    private ElementDictionary dict;
    
    private LinkedList<String> queryKeys = new LinkedList<String>();
    
    private ArrayList<String> matches = new ArrayList<String>();
    
    private ArrayList<String> suggestions = new ArrayList<String>();
    
    private HashMap<String, List<String>> camelCaseMap = new HashMap<String, List<String>>();
    
    private static ResourceBundle rb = ResourceBundle
            .getBundle("org.dcm4che3.tool.dcmdict.messages");
    
    public DcmDict() {}

    private static CommandLine parseComandLine(String[] args)
            throws ParseException {
        options = new Options();
        options.addOption(null, "private-creator", true, rb.getString("private-creator"));
        CLIUtils.addCommonOptions(options);
        return CLIUtils.parseComandLine(args,options, rb, DcmDict.class);
    }

    @SuppressWarnings("unchecked")
    public static void main(String[] args) {
        CommandLine cl = null;
        try {
            DcmDict main = new DcmDict();
            cl = parseComandLine(args);
            
            if(cl.hasOption("private-creator")) {
                String creator = cl.getOptionValue("private-creator");
                main.dict = ElementDictionary.getElementDictionary(creator);
            }
            else {
                main.dict = ElementDictionary.getStandardElementDictionary();
            }
            main.queryKeys = (LinkedList<String>) cl.getArgList();
            if(hasAbbreviation(main.queryKeys)) {
                buildCamelCaseMap(main);
            }
            buildMatches(main);
            if(!main.matches.isEmpty()) {
                printMatches(main);
            }
            if(!main.queryKeys.isEmpty()) {
                buildSuggestions(main);
                System.out.println(hashtag());
                printSuggestions(main);
            }
        } catch (ParseException e) {
            LOG.error("dcmdict\t" + e.getMessage());
            System.err.println(rb.getString("try"));
            System.exit(2);
        }
    }

    private static void buildCamelCaseMap(DcmDict main) {
        for(Field field : main.dict.getTagClass().getFields()) {
            if(main.camelCaseMap.get(getAbbreviation(getName(field.getName())))!=null) { 
                main.camelCaseMap.get(getAbbreviation(getName(field.getName()))).add(field.getName());
            }
            else {
            ArrayList<String> tmp = new ArrayList<String>();
            tmp.add(field.getName());
                main.camelCaseMap.put(getAbbreviation(getName(field.getName())), tmp);
            }
        }
        for(Field field : UID.class.getFields()) {
            if(main.camelCaseMap.get(getAbbreviation(getName(field.getName())))!=null) { 
                main.camelCaseMap.get(getAbbreviation(getName(field.getName()))).add(field.getName());
            }
            else {
            ArrayList<String> tmp = new ArrayList<String>();
            tmp.add(field.getName());
                main.camelCaseMap.put(getAbbreviation(getName(field.getName())), tmp);
            }
        }
    }

    private static boolean hasAbbreviation(LinkedList<String> queryKeys) {
        for(String str : queryKeys) {
            if(str.matches("[A-Z]+")) {
                return true;
            }
        }
        return false;
    }

    private static void buildSuggestions(DcmDict main) {
        for(String key : main.queryKeys) {
            if(isTag(key)) {
                System.out.format("%-120s\n",hashtag());
                System.out.format("%-120s\n","Illegal argument -> "+key+" tags must be complete");
            }
            else if(isUID(key)) {
                System.out.format("%-120s\n",hashtag());
                System.out.format("%-120s\n","Illegal argument -> "+key+" uids must be complete");
            }
            else {
                if(key.matches("[A-Z]+")) {
                    for(String keyStr : main.camelCaseMap.keySet()) {
                        if(keyStr.startsWith(key)) {
                            for(String str : main.camelCaseMap.get(keyStr))
                            main.suggestions.add(key + "\t"+str);
                        }
                    }
                    continue;
                }
            for(Field field : main.dict.getTagClass().getFields()) {
                if(field.getName().toLowerCase().startsWith(key.toLowerCase()))
                    main.suggestions.add(key + "\t"+field.getName());
            }
            for(Field field : UID.class.getFields()) {
                if(field.getName().toLowerCase().startsWith(key.toLowerCase()))
                    main.suggestions.add(key + "\t"+field.getName());
            }
            }
        }
    }

    private static void buildMatches(DcmDict main) {
        for(Iterator<String> iter = main.queryKeys.iterator(); iter.hasNext();) {
            String key = iter.next();
            
            if(isTag(key)) {
                key = key.replaceFirst("^0+(?!$)", "");
                int tagInDecimal = Integer.parseInt(key, 16);
                String keyWord = main.dict.keywordOf(tagInDecimal);
                String vr;
                String name;
                if(!keyWord.isEmpty()) {
                    vr = main.dict.vrOf(tagInDecimal).toString();
                    name = getName(keyWord);
                    iter.remove();
                    main.matches.add(name+"\t"+keyWord+"\t"+adjustHex(key)+"\t"+vr);
                }
                
            }
            else if(isUID(key)) {
                String name;
                try{
                    name = UID.nameOf(key);
                    if(!name.equalsIgnoreCase("?")) {
                    iter.remove();
                    main.matches.add(name+"\t"+name.replaceAll("\\s+","")+"\t"+key+"\t"+"-");
                    }
                }
                catch(IllegalArgumentException e) {
                    //
                }
            }
            else {
                if(key.matches("[A-Z]+")) {
                    if(main.camelCaseMap.get(key)!=null && main.camelCaseMap.get(key).size() == 1)
                    key = main.camelCaseMap.get(key).get(0);
                }
                int tag = main.dict.tagForKeyword(key);
                String vr;
                String name;
                if(tag == -1) {
                    try{
                        String uidStr = UID.forName(key);
                        name = UID.nameOf(uidStr);
                        iter.remove();
                        main.matches.add(name+"\t"+key+"\t"+uidStr+"\t"+"-");
                    }
                    catch(IllegalArgumentException e) {
                        //
                    }
                }
                else {
                    String hex = Long.toHexString(tag);
                    String tagStr = adjustHex(hex);
                    vr = main.dict.vrOf(tag).toString();
                    name = getName(key);
                    iter.remove();
                    main.matches.add(name+"\t"+key+"\t"+tagStr+"\t"+vr);
                }
                
            }
        }
    }

    private static boolean isUID(String key) {
        return key.matches("[012]((\\.0)|(\\.[1-9]\\d*))+");
    }

    private static String adjustHex(String hex) {
        String str = "";
            for(int i=8-hex.length();i>0;i--) {
                str+="0";
            }
            str+=hex;
            return str.toUpperCase();
    }

    private static String getName(String key) {
        String name = "";
        for (String w : key.split("(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])")) {
            name+=w+" ";
        }
        return name.trim();
    }
    
    private static String getAbbreviation(String name){
        String str = "";
        for(int i=0;i<name.split(" ").length;i++) {
            str+=name.split(" ")[i].charAt(0);
        }
        return str.toUpperCase();
    }
    private static boolean isTag(String key) {
        if(key.matches("[0-9a-fA-F]+")) {
            return true;
        }
        return false;
    }

    private static String hashtag() {
        String out = "";
        for(int i=0;i<140;i++) {
            out+="#";
        }
        return out;
    }

    private static void printSuggestions(DcmDict main) {
        String lastCurrent = null;
        for(String str : main.suggestions) {
            if(lastCurrent==null) {
            lastCurrent= str.split("\t")[0];
            System.out.format("%-120s\n","Query Key ->"+str.split("\t")[0]+" can be :");
            }
            if(!lastCurrent.equalsIgnoreCase(str.split("\t")[0])){
                System.out.format("%140s\n",hashtag());
                System.out.format("%-120s\n","Query Key ->"+str.split("\t")[0]+" can be :");
                lastCurrent= str.split("\t")[0];
        }
            System.out.format("%-120s\n",str.split("\t")[1]);
        }
    }

    private static void printMatches(DcmDict main) {
        System.out.format("%140s\n",hashtag());
        System.out.format("%-50s%-50s%-30s%-10s\n","Name","Keyword","Tag/UID","VR");
        System.out.format("%140s\n",hashtag());
        for(String str : main.matches)
            System.out.format("%-50s%-50s%-30s%-10s\n",str.split("\t")[0]
                    ,str.split("\t")[1],str.split("\t")[2],str.split("\t")[3]);
    }

}