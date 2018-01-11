// ConfigInterface.aidl
package com.example.epubreader.config;

import java.util.List;
// Declare any non-default types here with import statements

interface ConfigInterface {
//    List<String> listGroup();
//       List<String> listName(in String group);

//       String getValue(in String group, in String name);
       String getValue( in String name);
//       void setValue(in String group, in String name, in String value);
       void setValue(in String name, in String value);
//       void unsetValue(in String group, in String name);
//       void removeGroup(in String name);
//
//       List<String> requestAllValueForGroup(in String group);
}