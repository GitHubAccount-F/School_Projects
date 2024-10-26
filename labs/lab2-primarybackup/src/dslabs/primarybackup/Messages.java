package dslabs.primarybackup;

import dslabs.atmostonce.AMOApplication;
import dslabs.atmostonce.AMOCommand;
import dslabs.atmostonce.AMOResult;
import dslabs.framework.Address;
import dslabs.framework.Application;
import dslabs.framework.Message;
import lombok.Data;

/* -----------------------------------------------------------------------------------------------
 *  ViewServer Messages
 * ---------------------------------------------------------------------------------------------*/
@Data
class Ping implements Message {
  private final int viewNum;
}

@Data
class GetView implements Message {}

@Data
class ViewReply implements Message {
  private final View view;
}

/* -----------------------------------------------------------------------------------------------
 *  Primary-Backup Messages
 * ---------------------------------------------------------------------------------------------*/
@Data
class Request implements Message {
  // Your code here...
  private final AMOCommand amoCommand;
  private final int viewNum;
}

@Data
class Reply implements Message {
  // Your code here...
  private final AMOResult amoResult;
  private final int viewNum;
}

// Your code here...

@Data
class ForwardRequest implements Message {
  private final Request request;
}

@Data
class ForwardReply implements Message {
  private final boolean success;
  private final ForwardRequest forwardRequest;
}

@Data
class StateTransfer implements Message {
  private final AMOApplication application;
  private final View view;
}

@Data
class StateTransferAck implements Message {
  private final boolean success;
  private final View view;
}



