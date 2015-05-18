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

      Currently supported reference targets are `Device`s and `Connection`s within same device.

- A configurable property should not be related to volatile operational data, i.e., it should be something that changes not so often, a rule of thumb is one hour - if you expect that a property could generally change more often - choose a different way of storing it.

- Keep field/property declarations right:e.g., use `List` not `ArrayList`, put the right generic parameters, i.e., `List<ApplicationEntity>`
- Correctly named getters and setters MUST be provided for all configurable properties.


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
