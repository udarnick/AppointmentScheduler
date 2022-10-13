package com.example.boivin.appointmentscheduler.dao.user.provider;

import com.example.boivin.appointmentscheduler.dao.user.CommonUserRepository;
import com.example.boivin.appointmentscheduler.entity.Work;
import com.example.boivin.appointmentscheduler.entity.user.provider.Provider;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProviderRepository extends CommonUserRepository<Provider> {

    List<Provider> findByWorks(Work work);

    @Query("select distinct p from Provider p inner join p.works w where w.targetCustomer = 'retail'")
    List<Provider> findAllWithRetailWorks();

    @Query("select distinct p from Provider p inner join p.works w where w.targetCustomer = 'corporate'")
    List<Provider> findAllWithCorporateWorks();
}
