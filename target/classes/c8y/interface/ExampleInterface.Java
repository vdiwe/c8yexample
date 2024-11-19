package c8y

interface ExampleInterface {  
      

    private String enterpriseTenantId;

    private ExampleService deviceService

    public ResponseEntity<List<String>> getAllDeviceNames();

} 