package com.ecommerce.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Component

//This utility is not used anymore,Spring security encapsulates the logic
public class UserContext {
      private Long userId;
      private String role;

      public Boolean isGuest(){
            return userId==null;
      }
      public Boolean isAdmin(){
            return role.equals("ADMIN");
      }
      public Boolean isClient(){
            return role.equals("CLIENT");
      }
}
