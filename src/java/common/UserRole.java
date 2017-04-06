
package common;

import java.io.Serializable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import utilities.Debug;

/**
 * Represents the role with which a <code> User </code> in this system. Different
 * users have different access levels and different levels of functionality and
 * responsibility within the system.  
 * 
 * @author cjones
 */
public enum UserRole implements Serializable{
    
    /**
     * The administrator for the entire program, has complete control of the
     * system. 
     */
    SystemAdmin("SystemAdmin"),
    
 
    
    /**
     * A student at the university.
     */
    Student("Student");
    
    private final String roleName;
    
    /**
     * Constructs a <code> UserRole </code> given a role name. A user role
     * represents their position in the college and does not require them to be
     * a member of the <code> Faculty </code>.
     * 
     * @param roleName The name of this <code> UserRole </code> position.
     */
    UserRole(String roleName){
        this.roleName=roleName.trim();
    }
    
    /**
     * Returns the <code> UserRole </code> of the given role name. If the role name
     * given is not one of the roles in the enumerated type, the default is null."
     * 
     * @param roleName The name of this role, preferably one already in the enumerated type
     * (SystemAdmin, Administrator, Chair, Advisor, Instructor, Staff, or Student).
     * 
     * @return The <code> UserRole </code> corresponding to the role name given.
     */
     public static UserRole getUserRole (String roleName) {
        UserRole name = null;

        for (UserRole e : UserRole.values ()) {
            if (e.roleName.equalsIgnoreCase(roleName.trim())){
                name = e;
                break;
            }
        }
        return (name);
    }
     
    public static JSONObject getUserRoles () {
        JSONObject roles = new JSONObject();
        JSONArray rolesArr = new JSONArray();
        JSONObject role;
        for (UserRole e : UserRole.values ()) {
            role = new JSONObject();
            role.put("UserRole", e.getRoleName());
            rolesArr.add(role);
        }
        roles.put("UserRoles", rolesArr);
        return roles;
    }

     /**
     * Returns the role name of this <code> UserRole </code>.
     * 
     * @return This <code> UserRole </code>'s name.
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * Returns this <code> UserRole </code>'s name as a string.
     * 
     * @return The name of this <code> UserRole </code> as a string.
     */
    @Override
    public String toString() {
        return "UserRole{" + "roleName=" + roleName + '}';
    }
    
 }
