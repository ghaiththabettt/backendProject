package com.bezkoder.springjwt.HRModuleServices;

import com.bezkoder.springjwt.dtos.HRModuleDtos.PerksDTO;

import java.util.List;

public interface IPerksService {
    PerksDTO createPerks(PerksDTO perksDTO);

    PerksDTO updatePerks(Long perksId, PerksDTO perksDTO);

    void deletePerks(Long perksId);

    List<PerksDTO> getAllPerks();

    PerksDTO getPerksById(Long perksId);
}
