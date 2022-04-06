package com.probe.sdk.models;

  class CallBackOnMitigationConfChange {
    private static CallBackOnMitigationConfChange callBackOnMitigationConfChange;

    private interfaceOnMitigationConfChange interfaceMitigationChange;

    public static CallBackOnMitigationConfChange getInstance() {
        if (callBackOnMitigationConfChange == null) {
            callBackOnMitigationConfChange = new CallBackOnMitigationConfChange();
        }

        return callBackOnMitigationConfChange;
    }

    private CallBackOnMitigationConfChange() {

    }

    public interface interfaceOnMitigationConfChange {


        void updateAfterMitigationConfChange(String mealID);


    }

    public void initMitigationChangeCallback(interfaceOnMitigationConfChange
                                                     interfaceMitigationChange) {
        this.interfaceMitigationChange = interfaceMitigationChange;

    }

    public void notifyOnMitigationConfChange(String mealID) {
        if (interfaceMitigationChange != null) {
            interfaceMitigationChange.updateAfterMitigationConfChange(mealID);
        }
    }


    public void releaseCallback() {
        callBackOnMitigationConfChange = null;
        interfaceMitigationChange = null;

    }


}

