# Dcm4che configuration framework

### Features

1. Use `@ConfigurableClass` and `@ConfigurableProperty` to annotate configuration objects.
2. Framework allows to load these objects for you from a pre-configured storage (Ldap, json, preferences) and to save it back.
3. Configuration UI can be auto-generated based on the annotations (they can include description, validation rules, etc)

### As a component developer who needs to make things configurable

- Use `@ConfigurableClass` and `@ConfigurableProperty` annotations to mark configuration classes (think of an analogy to JPA's Entities and Fields).
    A property can be
    - any primitive, enum
    - array, collection, map of primitives
    - another configurable object
    - a collection, map of configurable objects
    - a reference, collection of references
    
      CAUTION: this feature's use should be limited to reduce the referential complexity - the more references are
      introduced, the more complex UI's logic will need to be to handle proper cascading

      Currently supported reference targets are `Device`s, `Connection`s, and `ApplicationEntity`s.
      
    - a special extensions maps (see below)

- A configurable property should not be related to volatile operational data, i.e., it should be something that changes not so often, a rule of thumb is one hour - if you expect that a property could generally change more often - choose a different way of storing it.

- Keep field/property declarations right:e.g., use `List` not `ArrayList`, put the right generic parameters, i.e., `List<ApplicationEntity>`
- Correctly named getters and setters MUST be provided for all configurable properties.

#### Config extensions (extensibility by composition)
Some properties might be related only to a certain, possibly optional feature of a configurable object, and then it makes sense not to include them into the main class but rather make the class extensible and put those properties into an (optional) extension. This also helps to avoid dependencies on the irrelevant parts (i.e. irrelevant extensions) of the configuration, while still allowing to manipulate the primary (extensible) objects. 

The framework allows extensibility by composition (i.e. not using polymorphism like A extends B, but rather adding 'features' to A, like A can have feature B and feature C). One can make a class 'extendable' by including a special property in a form of a Map where an extension class corresponds to an extension instance itself. A property must be marked with `type = ConfigurableProperty.ConfigurablePropertyType.ExtensionsProperty` to enable the extension logic.     
    
To make the framework aware of extensions while loading/persisting configuration, one has to register them (see `DicomConfigurationBuilder`). For EE case, there is a helper in `dcm4chee-conf-cdi` project that performs it automatically by using CDI. 
    
More examples:
- [Device](https://github.com/dcm4che/dcm4che/blob/master/dcm4che-net/src/main/java/org/dcm4che3/net/Device.java) and 
a device extension [ArchiveDeviceExtension](https://github.com/dcm4che/dcm4chee-arc-cdi/blob/master/dcm4chee-arc-conf/src/main/java/org/dcm4chee/archive/conf/ArchiveDeviceExtension.java).
- [How to use config extensions](https://github.com/dcm4che/dcm4chee-integration-examples/tree/master/config-extensions-example)


### As a tools/config provider developer

- Use [ConfigurationManager](https://github.com/dcm4che/dcm4che/blob/master/dcm4che-conf/dcm4che-conf-core/src/main/java/org/dcm4che3/conf/core/ConfigurationManager.java) to access the `Configuration` and `BeanVitalizer`. 
- Use [Configuration](https://github.com/dcm4che/dcm4che/blob/master/dcm4che-conf/dcm4che-conf-core-api/src/main/java/org/dcm4che3/conf/core/api/Configuration.java) API to load/persist configuration nodes (see Configuration storage init).
- Use [BeanVitalizer](https://github.com/dcm4che/dcm4che/blob/master/dcm4che-conf/dcm4che-conf-core/src/main/java/org/dcm4che3/conf/core/BeanVitalizer.java) to 'vitalize' objects, i.e. to fill in all the configurable properties of a bean using the loaded JSON configuration node/convert back to JSON node
- DO NOT make a custom representation for configurable classes, it will break things. Use references instead.


### As a UI developer

See [https://github.com/dcm4che/dcm4chee-conf-web](https://github.com/dcm4che/dcm4chee-conf-web)

Rely on JSON-schema


#### Notes

All the objects/maps in json are represented by TreeMap, so the order is kept consistent.





# Dicom Configuration interface

Dicom configuration ([DicomConfiguration](https://github.com/dcm4che/dcm4che/blob/master/dcm4che-conf/dcm4che-conf-api/src/main/java/org/dcm4che3/conf/api/DicomConfiguration.java)) is a registry of dicom [Devices](https://github.com/dcm4che/dcm4che/blob/master/dcm4che-net/src/main/java/org/dcm4che3/net/Device.java).

Each device has a set of `Connection`s and `ApplicationEntity`s.
 
One can extend configuration structure with `DeviceExtension`s, `AEExtension`s and `HL7ApplicationExtension`s. For example, see [StorageDeviceExtension](https://github.com/dcm4che/dcm4chee-storage2/blob/master/dcm4chee-storage-conf/src/main/java/org/dcm4chee/storage/conf/StorageDeviceExtension.java).


### Implementation of DicomConfiguration with dcm4che config framework

`CommonDicomConfigurationWithHL7` implements the `DicomConfiguration` interface using the dcm4che config framework.
This class also implements `ConfigurationManager` interface and therefore enables the access to the config on both `DicomConfiguration` and `Configuration` layers. One can initialize it using the `DicomConfigurationBuilder`.

### DicomConfigurationBuilder

Use `DicomConfigurationBuilder` to initialize DicomConfiguration (which also implements `ConfigurationManager` interface) with specific settings that can be either read from a `Properties` object or specified programmatically. 
For example, see [DicomConfigurationProducer](https://github.com/dcm4che/dcm4chee-arc-cdi/blob/master/dcm4chee-arc-conf-producer/src/main/java/org/dcm4chee/archive/conf/producer/DicomConfigurationProducer.java)
 or [ArchiveDeviceTest](https://github.com/dcm4che/dcm4chee-arc-cdi/blob/master/dcm4chee-arc-conf-test/src/test/java/org/dcm4chee/archive/conf/ArchiveDeviceTest.java).
