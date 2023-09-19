package c8y.service;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import c8y.IsDevice;
import com.cumulocity.rest.representation.inventory.ManagedObjectRepresentation;
import com.cumulocity.sdk.client.SDKException;
import com.cumulocity.sdk.client.inventory.InventoryApi;
import com.cumulocity.sdk.client.inventory.InventoryFilter;
import com.cumulocity.sdk.client.inventory.ManagedObjectCollection;
import com.google.common.collect.Lists;

/**
 * This is an example service. This should be removed for your real project!
 * 
 * @author APES
 *
 */

 @Service
 public class ExampleService {
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ExampleService.class);
    
    @Autowired
    private final InventoryApi inventoryApi;

    @Autowired
    public ExampleService(InventoryApi inventoryApi) {
        this.inventoryApi = inventoryApi;
    }

     public List<String> getAllDeviceNames(){
        List<String> allDeviceNames = new ArrayList<>();

        try {
            InventoryFilter inventoryFilter = new InventoryFilter();
            inventoryFilter.byFragmentType(IsDevice.class);

            ManagedObjectCollection managedObjectsByFilter = inventoryApi.getManagedObjectsByFilter(inventoryFilter);
            List<ManagedObjectRepresentation> allObjects = Lists.newArrayList(managedObjectsByFilter.get(2000).allPages());

            for (ManagedObjectRepresentation managedObjectRepresentation : allObjects) {
                allDeviceNames.add(managedObjectRepresentation.getName());
            }
        } catch (SDKException exception) {
            LOG.error("Error while loading devices from Cumulocity", exception);
        }

        return allDeviceNames;
    }
 }