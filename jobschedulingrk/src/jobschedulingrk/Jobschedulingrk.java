/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package algo.FCFS;

import GUI_Base.FinalResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


import org.cloudbus.cloudsim.Cloudlet;

import org.cloudbus.cloudsim.DatacenterBroker;
import org.cloudbus.cloudsim.DatacenterCharacteristics;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.ResCloudlet;
import org.cloudbus.cloudsim.UtilizationModel;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;


public class FCFS_Broker extends DatacenterBroker 
{
	DatacenterCharacteristics characteristics ;
        ArrayList parameters=new ArrayList();
        StringBuffer finalresult=new StringBuffer();
        protected void submitCloudlets() 
        {
		
                int vmIndex = 0;
                int pesNumber=1;
		ArrayList<Vm> Vmar2  =(ArrayList<Vm>) getVmsCreatedList();
                ArrayList<Cloudlet> Cloudar  =(ArrayList<Cloudlet>) getCloudletList();
                System.out.print("\n\n");
               double totalamt =0.0;
               double totalprocessingtime=0.0;
               double waitingtime=0.0;
               int i=1;
               int k=0;
		for (Cloudlet cloudlet : Cloudar) {
			Vm vm;
			vm = Vmar2.get(vmIndex);
			 System.out.println("=======>cloudlet length = " + cloudlet.getCloudletLength()); 
			Log.printLine(CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
                        finalresult.append("\ncloudlet length = " + cloudlet.getCloudletLength()); 
                       
			finalresult.append("\n"+CloudSim.clock() + ": " + getName() + ": Sending cloudlet "
					+ cloudlet.getCloudletId() + " to VM #" + vm.getId());
		
			cloudlet.setVmId(vm.getId());
			sendNow(getVmsToDatacentersMap().get(vm.getId()), CloudSimTags.CLOUDLET_SUBMIT, cloudlet);
			cloudletsSubmitted++;
			 vmIndex++;
                        if(vmIndex>=Vmar2.size())
                        {
                            vmIndex=0;
                            k=0;
                        }
                        getCloudletSubmittedList().add(cloudlet);
                        double processingtime = cloudlet.getCloudletLength() / vm.getMips()*vm.getNumberOfPes() +Math.random();   ;
                        totalprocessingtime += processingtime;
                        Log.printLine("Processing time : " + processingtime );
                        finalresult.append("\nProcessing time : " + processingtime );
                       double amt=0.0;
                       amt = characteristics.getCostPerMem() * vm.getRam();
                        amt += characteristics.getCostPerStorage() * vm.getSize();
                        totalamt += amt;
                        Log.printLine("Processing Cost : " + amt);
                         finalresult.append("\nProcessing Cost : " + amt);
                        waitingtime += cloudlet.getWaitingTime() + Math.random();
                          i++;
                                }
                   Log.printLine("total Processing time : " + totalprocessingtime);
                   Log.printLine("total Processing Cost : " + totalamt);
                 parameters.add(totalprocessingtime);
                  parameters.add(totalamt);
                   waitingtime = waitingtime/i;
                  Log.printLine("Average Waiting Time : " + waitingtime);
                     parameters.add(waitingtime);
                for (Cloudlet c1 : getCloudletSubmittedList()) {
			getCloudletList().remove(c1);
		}
                   FinalResult v=new FinalResult(finalresult.toString(),parameters);
                   v.setVisible(true);
	
	}
        protected void prcsVmCreate(SimEvent ev) {
		int[] data = (int[]) ev.getData();
		int datacenterId = data[0];
		int vmId = data[1];
		int result = data[2];

		
		incrementVmsAcks();

		
		if (getVmsCreatedList().size() == getVmList().size() - getVmsDestroyed()) {
			submitCloudlets();
		} else {
			
			if (getVmsRequested() == getVmsAcks()) {
				
				for (int nextDatacenterId : getDatacenterIdsList()) {
					if (!getDatacenterRequestedIdsList().contains(nextDatacenterId)) {
						createVmsInDatacenter(nextDatacenterId);
						return;
					}
				}

				
				if (getVmsCreatedList().size() > 0) {
					submitCloudlets();
				} else {
					Log.printLine(CloudSim.clock() + ": " + getName()
							+ ": none of the required VMs could be created. Aborting");
					finishExecution();
				}
			}
		}
	}



     
        public static  double getGs()
        {
            double j=1 * Math.random();

            return j;
        }
          public String getFinalResults()
        {
            return finalresult.toString();
        
        } 
	/*public void processEvent(SimEvent ev) {
           

            
		switch (ev.getTag()) {
		
			case CloudSimTags.RESOURCE_CHARACTERISTICS_REQUEST:
				processResourceCharacteristicsRequest(ev);
				break;



			case CloudSimTags.RESOURCE_CHARACTERISTICS:
				processResourceCharacteristics(ev);
				break;
			
			case CloudSimTags.VM_CREATE_ACK:
				processVmCreate(ev);
				break;
			
			case CloudSimTags.CLOUDLET_RETURN:
				processCloudletReturn(ev);
				break;
			
			case CloudSimTags.END_OF_SIMULATION:
				shutdownEntity();
				break;
                        case CloudSimTags.CLOUDLET_SUBMIT:
                             for(int i=0;i<getCloudletList().size();i++){ 
                         double rand = Math.random()*480;
                      getCloudletSubmittedList().get(i).setExecStartTime(rand);
		}
                            break;



                            
			default:
				processOtherEvent(ev);
				break;
		}
	}*/
        public FCFS_Broker(String name) throws Exception {
		super(name);
	}

	@Override
	protected void processResourceCharacteristics(SimEvent ev) {
		 characteristics = (DatacenterCharacteristics) ev.getData();
		getDatacenterCharacteristicsList().put(characteristics.getId(), characteristics);

		if (getDatacenterCharacteristicsList().size() == getDatacenterIdsList().size()) {
			createVmsInDatacenter(getDatacenterIdsList());
		}
	};

	protected void createVmsInDatacenter(List<Integer> datacenterIds) {


		int requestedVms = 0;
		int i = 0;
		for (Vm vm : getVmList()) {

			int datacenterId = datacenterIds.get(i++ % datacenterIds.size());
			String datacenterName = CloudSim.getEntityName(datacenterId);

			if (!getVmsToDatacentersMap().containsKey(vm.getId())) {
				Log.printLine(CloudSim.clock() + ": " + getName() + ": V r  Going  to Create VM #" + vm.getId() + " in " + datacenterName);
				sendNow(datacenterId, CloudSimTags.VM_CREATE_ACK, vm);
				requestedVms++;
			}
		}

		setVmsRequested(requestedVms);
		setVmsAcks(0);
	};
}/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jobschedulingrk;

/**
 *
 * @author Ashwani Rai
 */
public class Jobschedulingrk {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}
