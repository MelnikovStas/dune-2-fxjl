package com.dune.rts;

import com.almasb.fxgl.entity.component.Component;

public class BuildingComponent extends Component {
    
    private DuneRTSApp.BuildingType type;
    private String owner;
    
    public BuildingComponent(DuneRTSApp.BuildingType type, String owner) {
        this.type = type;
        this.owner = owner;
    }
    
    public DuneRTSApp.BuildingType getType() {
        return type;
    }
    
    public String getOwner() {
        return owner;
    }
}
