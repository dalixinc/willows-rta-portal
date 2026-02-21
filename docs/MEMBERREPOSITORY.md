# MemberRepository Required Change

## Add This Method

**File:** `src/main/java/com/willows/rta/repository/MemberRepository.java`

**Add this method to the interface:**

```java
/**
 * Count active members by address containing block name (case-insensitive)
 */
int countByMembershipStatusAndAddressContainingIgnoreCase(String membershipStatus, String addressFragment);
```

## Why This Is Needed

BlockService uses this method to count how many ACTIVE members live in each block by matching the block name against member addresses.

Example:
- Block name: "Windings House"
- Member address: "12A Windings House, The Willows"
- Match: YES → Count this member

## Full Example

```java
package com.willows.rta.repository;

import com.willows.rta.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    
    // ... existing methods ...
    
    /**
     * Count active members by address containing block name (case-insensitive)
     */
    int countByMembershipStatusAndAddressContainingIgnoreCase(String membershipStatus, String addressFragment);
}
```

## That's It!

Spring Data JPA will automatically implement this method based on the naming convention.
