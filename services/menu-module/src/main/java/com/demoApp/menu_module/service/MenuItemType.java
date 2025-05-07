package com.demoApp.menu_module.service;

import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for handling operations related to MenuItemType enum.
 */
@Service
public class MenuItemType {
    
    /**
     * Get all available menu item types as a list.
     * @return List of all menu item types
     */
    public List<String> getAllMenuItemTypes() {
        return Arrays.stream(com.demoApp.menu_module.entity.MenuItemType.values())
                .map(Enum::name)
                .collect(Collectors.toList());
    }
    
    /**
     * Check if a menu item type is valid.
     * @param type The menu item type to check
     * @return true if the type is valid, false otherwise
     */
    public boolean isValidMenuItemType(String type) {
        try {
            com.demoApp.menu_module.entity.MenuItemType.valueOf(type.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Convert a string to a MenuItemType enum.
     * @param typeName The name of the menu item type
     * @return The corresponding MenuItemType enum value, or null if invalid
     */
    public com.demoApp.menu_module.entity.MenuItemType getMenuItemTypeFromString(String typeName) {
        try {
            return com.demoApp.menu_module.entity.MenuItemType.valueOf(typeName.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Get the appropriate display name for a menu item type.
     * @param type The MenuItemType enum value
     * @return User-friendly display name for the menu item type
     */
    public String getDisplayName(com.demoApp.menu_module.entity.MenuItemType type) {
        if (type == null) {
            return "";
        }
        
        // Convert enum names like MAIN_COURSE to "Main Course"
        return Arrays.stream(type.name().split("_"))
                .map(word -> word.substring(0, 1).toUpperCase() + word.substring(1).toLowerCase())
                .collect(Collectors.joining(" "));
    }
}
