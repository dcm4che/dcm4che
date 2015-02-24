Dcm4che configuration framework
----------------------------

Features
--------

1. Use `@ConfigurableClass` and `@ConfigurableProperty` to annotate configuration objects.
2. Framework allows to load these objects for you from a pre-configured storage (Ldap, json, preferences) and to save it back.
3. Configuration UI is auto-generated based on the annotations (they can include description, validation rules, etc)

As a user-developer
--------------------
- Just put annotations!
- Supported : all primitives, maps, collections, enums, references (see `ConfigurableProperty.collectionOfReferences`)
- Keep field/property declarations right:e.g., use `List` not `ArrayList`, put the right generic parameters, i.e., `List<ApplicationEntity>`
- Correctly named getters and setters MUST be provided for all configurable properties.


As a configuration provider developer
-------------------------------------
- Encapsulate all calls to Vitalizer, Adapters, and Configuration into some Configuration provider class like DicomConfguration!
- Use `Configuration` to load/persist configuration nodes.
- Use `BeanVitalizer` to 'vitalize' objects, i.e. to fill in all the configurable properties of a bean using the loaded configuration node.
- Provide RESTful services to load/persist configuration nodes, use ReflectiveAdapter.getSchema to generate JSON schema for a configurable class.
- DO NOT make a custom representation for configurable classes, it will break things. Use references instead.


As a UI developer
-----------------
Rely on JSON-schema



Notes
-----
All the objects/maps in json are represented by TreeMap, so the order is kept consistent.
