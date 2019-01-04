package eu.opends.codriver.util;

import java.util.Arrays;
import java.util.List;

import com.sun.jna.Pointer;
import com.sun.jna.Structure;


public class DataStructures
{
    public static class Input_data_str extends Structure
    {
    	public static class ByReference extends Input_data_str implements Structure.ByReference { }
    	public static class ByValue extends Input_data_str implements Structure.ByValue { }
    	
    	public int ID = 0; /* Enumeration 
    	01=SCN,
    	11=MNV,
    	21=LASER,
    	31=CAM_E2CALL,
    	41=MAP_E2CALL,
    	51=SPAT_E2CALL */
    	public int Version = 0; /* Identifies data structure */
    	public int CycleNumber = 0;
    	public int ECUtime = 0;
    	public int AVItime = 0; /* From DATALOG PC, CANape Multimedia 1 signal */
    	public double TimeStamp = 0; /* UTC time difference after 1st January 1970, obtained from GPS time with leap seconds (Unix epoch) */
    	public int Status = 0; /* 0 = ACTIVE */
    	public double VLgtFild = 0;
    	public double ALgtFild = 0;
    	public double ALatFild = 0;
    	public double YawRateFild = 0; /* Note that yaw-rate is the derivative of the heading, i.e. chassis rotation rate, not speed rotation rate */
    	public double SteerWhlAg = 0; /* Positive when the car is turning left */
    	public double SteerWhlAgSpd = 0; /* Derivative of steering wheel angle */
    	public double SteerTorque = 0;
    	public double EngineSpeed = 0;
    	public double MasterCylinderPressure = 0;
    	public double FuelConsumption = 0;
    	public double GasPedPos = 0;
    	public double EngineTorque = 0;
    	public double EngineFrictionTorque = 0;
    	public double MaxEngineTorque = 0;
    	public double EngineTorqueDriverReq = 0;
    	public double MaxEngineTorqueNorm = 0;
    	public int ExTemp = 0;
    	public int BrakePedalSwitchNCSts = 0; /* 0 = UNKNOWN; 1 = RELEASED; 2 = PRESSED */
    	public int ActGear = 0;
    	public int IndTurnComm = 0; /* 0 = UNKNOWN; 1 = OFF; 2 = LEFT; 3 = RIGHT */
    	public int VehicleID = 0;
    	public int VehicleType = 0;
    	public int VehicleLightsStatus = 0;
    	public double VehicleLen = 0;
    	public double VehicleWidth = 0;
    	public double VehicleBarLongPos = 0; /* Distance to front bumper */
    	public double RequestedCruisingSpeed = 0;
    	public int AutomationLevel = 0; /* 0 = NO AUTOMATION, 
    	1 = ASSISTED, 
    	2 = PARTIAL AUTOMATION, 
    	3 = CONDITIONAL AUTOMATION, 
    	4 = HIGH AUTOMATION, 
    	5 = FULL AUTOMATION, 
    	6 = UNKNOWN */
    	public int SystemStatus = 0;
    	public int SystemMode = 0;
    	public int CurrentLane = 0; /* Nomenclature from ADASIS: 0 = Unknown, 
    	1 = Emergency lane, 
    	2 = Single-lane road, 
    	3 = Left-most lane, 
    	4 = Right-most lane, 
    	5 = One of middle lanes on road with three or more lanes */
    	public int NrObjs = 0; /* Limited to 20 max number of objects, selection needed */
    	public int[] ObjID = new int[20];
    	public int[] ObjClass = new int[20]; /* unknown(0), 
    	pedestrian(1), 
    	cyclist(2), 
    	moped(3), 
    	motorcycle(4), 
    	passengerCar(5), 
    	bus(6), 
    	lightTruck(7), 
    	heavyTruck(8), 
    	trailer(9), 
    	specialVehicles(10), 
    	tram(11), 
    	roadSideUnit(15) */
    	public int[] ObjSensorInfo = new int[20]; /* xxxxxxxD = LIDAR, 
    	xxxxxxDx = CAMERA, 
    	xxxxxDxx = RADAR, 
    	xxxxDxxx = V2V, 
    	xxxDxxxx = Blind Spot SX, 
    	xxDxxxxx = Blind Spot DX. */
    	public double[] ObjX = new double[20]; /* Position of the barycentre */
    	public double[] ObjY = new double[20]; /* Position of the barycentre */
    	public double[] ObjLen = new double[20]; /* Along object speed direction, along vehicle axis for stationary obstacles. 0 means unknown. */
    	public double[] ObjWidth = new double[20]; /* Perpendicular to object speed direction, perpendicular to vehicle axis for stationary 
    	obstacles. 0 means unknown. */
    	public double[] ObjVel = new double[20]; /* Speed module, not longitudinal speed */
    	public double[] ObjCourse = new double[20]; /* In vehicle reference system */
    	public double[] ObjAcc = new double[20]; /* Tangential acceleration */
    	public double[] ObjCourseRate = new double[20];
    	public int[] ObjNContourPoints = new int[20]; /* Limited to 10 max number of contour points for each object */
    	public double[] ObjContourPoint1X = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint1Y = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint2X = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint2Y = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint3X = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint3Y = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint4X = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint4Y = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint5X = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint5Y = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint6X = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint6Y = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint7X = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint7Y = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint8X = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint8Y = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint9X = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint9Y = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint10X = new double[20]; /* In vehicle reference system */
    	public double[] ObjContourPoint10Y = new double[20]; /* In vehicle reference system */
    	public int[] ObjBrakePedalSwitchNCSts = new int[20]; /* 0 = UNKNOWN; 1 = RELEASED; 2 = PRESSED */
    	public int[] ObjIndTurnComm = new int[20]; /* 0 = UNKNOWN; 1 = OFF; 2 = LEFT; 3 = RIGHT */
    	public int[] ObjVehicleLightsStatus = new int[20];
    	public int[] ObjVehicleID = new int[20];
    	public int[] ObjVehicleDrivingMode = new int[20]; /* 0 = NO AUTOMATION, 
    	1 = ASSISTED, 
    	2 = PARTIAL AUTOMATION, 
    	3 = CONDITIONAL AUTOMATION, 
    	4 = HIGH AUTOMATION, 
    	5 = FULL AUTOMATION, 
    	6 = UNKNOWN */
    	public int[] ObjNTrajectoryPoints = new int[20]; /* Limited to 23 max number of trajectory  points for each object */
    	public double[] ObjTrajectoryPointITime = new double[460]; /* Unix epoch */
    	public double[] ObjTrajectoryPointIX = new double[460]; /* In vehicle reference system */
    	public double[] ObjTrajectoryPointIY = new double[460]; /* In vehicle reference system */
    	public double LaneWidth = 0;
    	public double LatOffsLaneR = 0;
    	public double LatOffsLaneL = 0;
    	public double LaneHeading = 0;
    	public double LaneCrvt = 0; /* Positive for left curves */
    	public double DetectionRange = 0;
    	public int LeftLineConf = 0;
    	public int RightLaneConf = 0;
    	public int LeftLaneType = 0;
    	public int RightLaneType = 0;
    	public double GpsLongitude = 0; /* As measured by GPS, East positive */
    	public double GpsLatitude = 0; /* As measured by GPS, North positive */
    	public double GpsSpeed = 0; /* As measured by GPS */
    	public double GpsCourse = 0; /* With respect to North, clockwise, as measured by GPS */
    	public double GpsHdop = 0; /* Diluition of precision as indicated by GPS */
    	public double EgoLongitude = 0; /* After position filter, referred to barycentre */
    	public double EgoLatitude = 0; /* After position filter, referred to barycentre */
    	public double EgoCourse = 0; /* With respect to North, clockwise, after position filter */
    	public double EgoDop = 0; /* Diluition of precision, after position filter */
    	public int FreeLaneLeft = 0; /* 0 = NOT AVAILABLE; 1 = FREE */
    	public int FreeLaneRight = 0; /* 0 = NOT AVAILABLE; 1 = FREE */
    	public int SideObstacleLeft = 0; /* 0 = NO OBSTACLE: 1 = OBSTACLE PRESENT */
    	public int SideObstacleRight = 0; /* 0 = NO OBSTACLE; 1 = OBSTACLE PRESENT */
    	public int BlindSpotObstacleLeft = 0; /* 0 = NO OBSTACLE; 1 = OBSTACLE PRESENT */
    	public int BlindSpotObstacleRight = 0; /* 0 = NO OBSTACLE; 1 = OBSTACLE PRESENT */
    	public int LeftAdjacentLane = 0; /* 0 = NOT DETECTED; 1 = DETECTED */
    	public int RightAdjacentLane = 0; /* 0 = NOT DETECTED; 1 = DETECTED */
    	public int NrPaths = 0; /* Currently only up to one path is transmitted, this can be extended in the future */
    	public int AdasisCoordinatesNr = 0; /* ADASIS description */
    	public double[] AdasisCoordinatesDist = new double[200];
    	public double[] AdasisLongitudeValues = new double[200]; /* For test purpose, only first point is necessary */
    	public double[] AdasisLatitudeValues = new double[200]; /* For test purpose, only first point is necessary */
    	public int AdasisHeadingChangeNr = 0;
    	public double[] AdasisHeadingChangeDist = new double[200];
    	public double[] AdasisHeadingChangeValues = new double[200]; /* See definition in ADASIS protocol */
    	public int AdasisCurvatureNr = 0;
    	public double[] AdasisCurvatureDist = new double[200];
    	public double[] AdasisCurvatureValues = new double[200]; /* Positive for left curves */
    	public int AdasisSpeedLimitNr = 0;
    	public double[] AdasisSpeedLimitDist = new double[20];
    	public int[] AdasisSpeedLimitValues = new int[20]; /* 0 means unknown */
    	public int AdasisSlopeNr = 0;
    	public double[] AdasisSlopeDist = new double[200];
    	public double[] AdasisSlopeValues = new double[200];
    	public int AdasisNLanesNr = 0;
    	public double[] AdasisNLanesDist = new double[20];
    	public int[] AdasisNLanesValues = new int[20];
    	public int AdasisLinkIdNr = 0;
    	public double[] AdasisLinkIdDist = new double[20];
    	public int[] AdasisLinkIdValues = new int[20];
    	public int PriorityLevelNr = 0;
    	public double[] PriorityLevelDist = new double[20];
    	public int[] PriorityLevelValues = new int[20]; /* Not directly available from ADASIS, derived from other info */
    	public int CompactDescriptionOrigin = 0; /* Possible sources:
    	0 = no map data available (only 1 segment is provided following the formula: max(1; v+0,2*v^2))
    	1 = HERE map without ADAS plugin
    	2 = HERE map with ADAS plugin
    	3 = eHorizon
    	4 = fully detailed trajectories map */
    	public double PathOriginLongitudeCompact = 0;
    	public double PathOriginLatitudeCompact = 0;
    	public int PathNrSegsCompact = 0; /* Compact description */
    	public double[] PathSegLenCompact = new double[20];
    	public double[] PathHeadingChangeCompact = new double[20]; /* The first one is the initial heading */
    	public double[] PathCurvatureCompact = new double[20];
    	public int[] PathSpeedLimitCompact = new int[20];
    	public double[] PathSlopeCompact = new double[20]; /* Banking will be added when available */
    	public int[] PathNLanesCompact = new int[20];
    	public int[] PathLinkIdCompact = new int[20];
    	public int[] PathPriorityLevelCompact = new int[20];
    	public int IntersectionID = 0; /* Note that often only the lower 16 bits of this value will be sent as the operational region (state etc) 
    	will be known and not sent each time.
    	ASN.1 Representation:
    	IntersectionID ::= OCTET STRING (SIZE(2..4)) */
    	public double IntersectionDistance = 0; /* Distance to intersection obtained from absolute positions of vehicle and intersection. 
    	To be calculated using the MAP message. */
    	public double IntersectionLatitude = 0; /* Providing a range of plus-minus 90 degrees */
    	public double IntersectionLongitude = 0; /* Providing a range of plus-minus 180 degrees */
    	public int TrfLightCurrState = 0; /* The value indicates the Signal Light State: 1 is Green, 2 is Yellow, 3 is Red and 0 is Flashing */
    	public int TrfLightFirstTimeToChange = 0; /* -- the point in time this state will change
		ASN.1 Representation:
    	TimeMark ::= INTEGER (0..12002)
    	-- In units of 1/10th second from local UTC time
    	-- A range of 0~600 for even minutes, 601~1200 for odd minutes
    	-- 12001 to indicate indefinite time
    	-- 12002 to be used when value undefined or unknown */
    	public int TrfLightFirstNextState = 0; /* The value indicates the Signal Light State: 1 is Green, 2 is Yellow, 3 is Red and 0 is Flashing */
    	public int TrfLightSecondTimeToChange = 0; /* ASN.1 Representation:
    	TimeMark ::= INTEGER (0..12002)
    	-- In units of 1/10th second from local UTC time
    	-- A range of 0~600 for even minutes, 601~1200 for odd minutes
    	-- 12001 to indicate indefinite time
    	-- 12002 to be used when value undefined or unknown */
    	public int TrfLightSecondNextState = 0; /* The value indicates the Signal Light State: 1 is Green, 2 is Yellow, 3 is Red and 0 is Flashing */
    	public int TrfLightThirdTimeToChange = 0; /* ASN.1 Representation:
    	TimeMark ::= INTEGER (0..12002)
    	-- In units of 1/10th second from local UTC time
    	-- A range of 0~600 for even minutes, 601~1200 for odd minutes
    	-- 12001 to indicate indefinite time
    	-- 12002 to be used when value undefined or unknown */
    	
    	
    	public Input_data_str(Pointer p)
    	{
    		super(p, Structure.ALIGN_NONE);
    		read();
    	}
    	
    	public Input_data_str()
    	{
    		super(Structure.ALIGN_NONE);
    	}
    	
        protected List<String> getFieldOrder() 
        { 
            return Arrays.asList(new String[] {
                "ID", "Version", "CycleNumber", "ECUtime", "AVItime", "TimeStamp", "Status", "VLgtFild", "ALgtFild",
                "ALatFild", "YawRateFild", "SteerWhlAg", "SteerWhlAgSpd", "SteerTorque", "EngineSpeed", 
                "MasterCylinderPressure", "FuelConsumption", "GasPedPos", "EngineTorque", "EngineFrictionTorque", 
                "MaxEngineTorque", "EngineTorqueDriverReq", "MaxEngineTorqueNorm", "ExTemp", "BrakePedalSwitchNCSts",
                "ActGear", "IndTurnComm", "VehicleID", "VehicleType", "VehicleLightsStatus", "VehicleLen", 
                "VehicleWidth", "VehicleBarLongPos", "RequestedCruisingSpeed", "AutomationLevel", "SystemStatus",
                "SystemMode", "CurrentLane", "NrObjs", "ObjID", "ObjClass", "ObjSensorInfo", "ObjX", "ObjY",
                "ObjLen", "ObjWidth", "ObjVel", "ObjCourse", "ObjAcc", "ObjCourseRate", "ObjNContourPoints", 
                "ObjContourPoint1X", "ObjContourPoint1Y", "ObjContourPoint2X", "ObjContourPoint2Y", "ObjContourPoint3X", 
                "ObjContourPoint3Y", "ObjContourPoint4X", "ObjContourPoint4Y", "ObjContourPoint5X", "ObjContourPoint5Y", 
                "ObjContourPoint6X", "ObjContourPoint6Y", "ObjContourPoint7X", "ObjContourPoint7Y", "ObjContourPoint8X",
                "ObjContourPoint8Y", "ObjContourPoint9X", "ObjContourPoint9Y", "ObjContourPoint10X", "ObjContourPoint10Y", 
                "ObjBrakePedalSwitchNCSts", "ObjIndTurnComm", "ObjVehicleLightsStatus", "ObjVehicleID", 
                "ObjVehicleDrivingMode", "ObjNTrajectoryPoints", "ObjTrajectoryPointITime", "ObjTrajectoryPointIX", 
                "ObjTrajectoryPointIY", "LaneWidth", "LatOffsLaneR", "LatOffsLaneL", "LaneHeading", 
                "LaneCrvt", "DetectionRange", "LeftLineConf", "RightLaneConf", "LeftLaneType", 
                "RightLaneType", "GpsLongitude", "GpsLatitude", "GpsSpeed", "GpsCourse", "GpsHdop",
                "EgoLongitude", "EgoLatitude", "EgoCourse", "EgoDop", "FreeLaneLeft", "FreeLaneRight",
                "SideObstacleLeft", "SideObstacleRight", "BlindSpotObstacleLeft", "BlindSpotObstacleRight",
                "LeftAdjacentLane", "RightAdjacentLane", "NrPaths", "AdasisCoordinatesNr", "AdasisCoordinatesDist",
                "AdasisLongitudeValues", "AdasisLatitudeValues", "AdasisHeadingChangeNr", "AdasisHeadingChangeDist", 
                "AdasisHeadingChangeValues", "AdasisCurvatureNr", "AdasisCurvatureDist", "AdasisCurvatureValues",
                "AdasisSpeedLimitNr", "AdasisSpeedLimitDist", "AdasisSpeedLimitValues", "AdasisSlopeNr", 
                "AdasisSlopeDist", "AdasisSlopeValues", "AdasisNLanesNr", "AdasisNLanesDist", "AdasisNLanesValues",
                "AdasisLinkIdNr", "AdasisLinkIdDist", "AdasisLinkIdValues", "PriorityLevelNr", "PriorityLevelDist",
                "PriorityLevelValues", "CompactDescriptionOrigin", "PathOriginLongitudeCompact", 
                "PathOriginLatitudeCompact", "PathNrSegsCompact", "PathSegLenCompact", "PathHeadingChangeCompact",
                "PathCurvatureCompact", "PathSpeedLimitCompact", "PathSlopeCompact", "PathNLanesCompact",
                "PathLinkIdCompact", "PathPriorityLevelCompact", "IntersectionID", "IntersectionDistance",
                "IntersectionLatitude", "IntersectionLongitude", "TrfLightCurrState", "TrfLightFirstTimeToChange",
                "TrfLightFirstNextState", "TrfLightSecondTimeToChange", "TrfLightSecondNextState", "TrfLightThirdTimeToChange"});
        }
    }
    
    
    
    public static class Output_data_str extends Structure
    {
    	public static class ByReference extends Output_data_str implements Structure.ByReference { }
    	public static class ByValue extends Output_data_str implements Structure.ByValue { }
    	
    	public int ID; /* Enumeration 
    	01=SCN,
    	11=MNV,
    	21=LASER,
    	31=CAM_E2CALL,
    	41=MAP_E2CALL,
    	51=SPAT_E2CALL */
    	public int Version; /* Identifies data structure */
    	public int CycleNumber;
    	public int ECUtime;
    	public int AVItime; /* From DATALOG PC, CANape Multimedia 1 signal */
    	public double TimeStamp; /* UTC time difference after 1st January 1970, obtained from GPS time with leap seconds (Unix epoch) */
    	public int Status; /* 0 = ACTIVE */
    	public int TimeHeadwayPolicyf; /* 0 = NOT COMPUTED
    	1 = GREEN
    	2 = YELLOW
    	3 = RED */
    	public int LegalSpeedPolicyf; /* 0 = NOT COMPUTED
    	1 = GREEN
    	2 = YELLOW
    	3 = RED */
    	public int LegalSpeedLimitf;
    	public int LandmarkPolicyf; /* 0 = NOT COMPUTED
    	1 = GREEN
    	2 = YELLOW
    	3 = RED */
    	public int LandmarkTypef; /* 0 = NO LANDMARK
    	1 = STOP SIGN
    	2 = PEDESTRIAN CROSSING
    	3 = YIELD SIGN
    	4 = SEMAPHORE */
    	public int AccelerationPolicyForCurvef; /* 0 = NOT COMPUTED
    	1 = GREEN
    	2 = YELLOW LEFT
    	3 = YELLOW RIGHT
    	4 = RED LEFT
    	5 = RED RIGHT */
    	public int RearTimeHeadwayPolicyLeftf; /* 0 = NOT COMPUTED
    	1 = GREEN
    	2 = YELLOW
    	3 = RED */
    	public int LeftThreatTypef; /* 0 = SIDE OBSTACLE; 1 = RUN OFF ROAD */
    	public int RearTimeHeadwayPolicyRightf; /* 0 = NOT COMPUTED
    	1 = GREEN
    	2 = YELLOW
    	3 = RED */
    	public int RightThreatTypef; /* 0 = SIDE OBSTACLE; 1 = RUN OFF ROAD */
    	public int LeftLanePolicyf; /* 0 = NOT COMPUTED
    	1 = GREEN
    	2 = YELLOW
    	3 = RED */
    	public int RightLanePolicyf; /* 0 = NOT COMPUTED
    	1 = GREEN
    	2 = YELLOW
    	3 = RED */
    	public int TravelTimePolicyf; /* 0 = NOT COMPUTED
    	1 = COMFORT
    	2 = NORMAL
    	3 = SPORT */
    	public int RecommendedGearf; /* 0 = NOT COMPUTED */
    	public int TimeHeadwayPolicys; /* 0 = NOT COMPUTED
    	1 = GREEN
    	2 = YELLOW
    	3 = RED */
    	public int LegalSpeedPolicys; /* 0 = NOT COMPUTED
    	1 = GREEN
    	2 = YELLOW
    	3 = RED */
    	public int LegalSpeedLimits;
    	public int LandmarkPolicys; /* 0 = NOT COMPUTED
    	1 = GREEN
    	2 = YELLOW
    	3 = RED */
    	public int LandmarkTypes; /* 0 = NO LANDMARK
    	1 = STOP SIGN
    	2 = PEDESTRIAN CROSSING
    	3 = YIELD SIGN
    	4 = SEMAPHORE */
    	public int AccelerationPolicyForCurves; /* 0 = NOT COMPUTED
    	1 = GREEN
    	2 = YELLOW LEFT
    	3 = YELLOW RIGHT
    	4 = RED LEFT
    	5 = RED RIGHT */
    	public int RearTimeHeadwayPolicyLefts; /* 0 = NOT COMPUTED
    	1 = GREEN
    	2 = YELLOW
    	3 = RED */
    	public int LeftThreatTypes; /* 0 = SIDE OBSTACLE; 1 = RUN OFF ROAD */
    	public int RearTimeHeadwayPolicyRights; /* 0 = NOT COMPUTED
    	1 = GREEN
    	2 = YELLOW
    	3 = RED */
    	public int RightThreatTypes; /* 0 = SIDE OBSTACLE; 1 = RUN OFF ROAD */
    	public int LeftLanePolicys; /* 0 = NOT COMPUTED
    	1 = GREEN
    	2 = YELLOW
    	3 = RED */
    	public int RightLanePolicys; /* 0 = NOT COMPUTED
    	1 = GREEN
    	2 = YELLOW
    	3 = RED */
    	public int TravelTimePolicys; /* 0 = NOT COMPUTED
    	1 = COMFORT
    	2 = NORMAL
    	3 = SPORT */
    	public int RecommendedGears; /* 0 = NOT COMPUTED */
    	public int TargetIDf;
    	public int TargetClassf; /* 0 = UNCLASSIFIED
    	1 = UNKNOWN SMALL
    	2 = UNKNOWN BIG
    	3 = PEDESTRIAN
    	4 = BIKE OR MOTORBIKE
    	5 = CAR
    	6 = TRUCK OR BUS */
    	public int TargetSensorInformationf; /* xxxxxxxD = LIDAR
    	xxxxxxDx = CAMERA
    	xxxxxDxx = RADAR
    	xxxxDxxx = V2V */
    	public double TargetXf;
    	public double TargetYf;
    	public double TargetDistancef; /* Distance to intersection obtained from absolute positions of vehicle and target */
    	public double TargetLengthf;
    	public double TargetWidthf;
    	public double TargetSpeedf; /* 0 means stationary */
    	public double TargetCoursef;
    	public double TargetAccelerationf;
    	public double TargetCourseRatef;
    	public int TargetDrivingModef; /* 0 = NO AUTOMATION, 
    	1 = ASSISTED, 
    	2 = PARTIAL AUTOMATION, 
    	3 = CONDITIONAL AUTOMATION, 
    	4 = HIGH AUTOMATION, 
    	5 = FULL AUTOMATION, 
    	6 = UNKNOWN */
    	public int TargetIDs;
    	public int TargetClasss; /* 0 = UNCLASSIFIED
    	1 = UNKNOWN SMALL
    	2 = UNKNOWN BIG
    	3 = PEDESTRIAN
    	4 = BIKE OR MOTORBIKE
    	5 = CAR
    	6 = TRUCK OR BUS */
    	public int TargetSensorInformations; /* xxxxxxxD = LIDAR
    	xxxxxxDx = CAMERA
    	xxxxxDxx = RADAR
    	xxxxDxxx = V2V */
    	public double TargetXs;
    	public double TargetYs;
    	public double TargetDistances; /* Distance to intersection obtained from absolute positions of vehicle and target */
    	public double TargetLengths;
    	public double TargetWidths;
    	public double TargetSpeeds; /* 0 means stationary */
    	public double TargetCourses;
    	public double TargetAccelerations;
    	public double TargetCourseRates;
    	public int TargetDrivingModes; /* 0 = NO AUTOMATION, 1 = ASSISTED, 2 = PARTIAL AUTOMATION, 3 = CONDITIONAL AUTOMATION, 4 = HIGH AUTOMATION, 5 = FULL AUTOMATION, 6 = UNKNOWN */
    	public int NTrajectoryPointsf; /* Limited to 23 max number of trajectory points */
    	public double[] TrajectoryPointITimef = new double[23]; /* Unix epoch */
    	public double[] TrajectoryPointIXf = new double[23]; /* In vehicle reference system */
    	public double[] TrajectoryPointIYf = new double[23]; /* In vehicle reference system */
    	public int NTrajectoryPointss; /* Limited to 23 max number of trajectory points */
    	public double[] TrajectoryPointITimes = new double[23]; /* Unix epoch */
    	public double[] TrajectoryPointIXs = new double[23]; /* In vehicle reference system */
    	public double[] TrajectoryPointIYs = new double[23]; /* In vehicle reference system */
    	public double T0; /* Absolute time when motion described by motor primitives starts. Unix epoch. */
    	public double V0; /* Longitudinal speed at the time of generation of thge motor primitive */
    	public double A0; /* Time derivative of speed, also valid for second trajectory */
    	public double T1f;
    	public double J0f; /* Time derivative of acceleration */
    	public double S0f; /* Time derivative of jerk */
    	public double Cr0f; /* Time derivative of snap */
    	public double T2f;
    	public double J1f; /* Time derivative of acceleration */
    	public double S1f; /* Time derivative of jerk */
    	public double Cr1f; /* Time derivative of snap */
    	public double Sn0; /* Also valid for second trajectory */
    	public double Alpha0; /* Also valid for second trajectory */
    	public double Delta0; /* Curvature of vehicle trajectory relative to lane curvature */
    	public double T1nf;
    	public double Jdelta0f;
    	public double Sdelta0f;
    	public double Crdelta0f;
    	public double T2nf;
    	public double Jdelta1f;
    	public double Sdelta1f;
    	public double Crdelta1f;
    	public double T1s;
    	public double J0s; /* Time derivative of acceleration */
    	public double S0s; /* Time derivative of jerk */
    	public double Cr0s; /* Time derivative of snap */
    	public double T2s;
    	public double J1s; /* Time derivative of acceleration */
    	public double S1s; /* Time derivative of jerk */
    	public double Cr1s; /* Time derivative of snap */
    	public double T1ns;
    	public double Jdelta0s;
    	public double Sdelta0s;
    	public double Crdelta0s;
    	public double T2ns;
    	public double Jdelta1s;
    	public double Sdelta1s;
    	public double Crdelta1s;
    	public int FirstManoeuverTypeLong; /* E.g: follow object, free flow, stopping, etc. */
    	public int FirstManoeuverTypeLat; /* E.g: lane keeping, lane change left, lane change right, etc. */
    	public int SecondManoeuverTypeLong; /* E.g: follow object, free flow, stopping, etc. */
    	public int SecondManoeuverTypeLat; /* E.g: lane keeping, lane change left, lane change right, etc. */
    	public double TargetSpeed; /* Speed of the vehicle at the end of the manoeuvre */
    	public double TargetLongitudinalAcceleration; /* Longitudinal acceleration required to perform the calculated manoeuvre */
    	public double TargetDistanceToPreceedingVehicle; /* Distance from the preceding vehicle at the end of the manoeuvre */
    	
    	public Output_data_str(Pointer p)
    	{
    		super(p, Structure.ALIGN_NONE);
    		read();
    	}
    	
    	public Output_data_str()
    	{
    		super(Structure.ALIGN_NONE);
    	}
    	
        protected List<String> getFieldOrder() 
        {
            return Arrays.asList(new String[] {
        	  "ID", "Version", "CycleNumber", "ECUtime", "AVItime", "TimeStamp", "Status", "TimeHeadwayPolicyf", 
        	  "LegalSpeedPolicyf", "LegalSpeedLimitf", "LandmarkPolicyf", "LandmarkTypef", "AccelerationPolicyForCurvef", 
        	  "RearTimeHeadwayPolicyLeftf", "LeftThreatTypef", "RearTimeHeadwayPolicyRightf", "RightThreatTypef", 
        	  "LeftLanePolicyf", "RightLanePolicyf", "TravelTimePolicyf", "RecommendedGearf", "TimeHeadwayPolicys", 
        	  "LegalSpeedPolicys", "LegalSpeedLimits", "LandmarkPolicys", "LandmarkTypes", "AccelerationPolicyForCurves", 
        	  "RearTimeHeadwayPolicyLefts", "LeftThreatTypes", "RearTimeHeadwayPolicyRights", "RightThreatTypes", 
        	  "LeftLanePolicys", "RightLanePolicys", "TravelTimePolicys", "RecommendedGears", "TargetIDf", "TargetClassf", 
        	  "TargetSensorInformationf", "TargetXf", "TargetYf", "TargetDistancef", "TargetLengthf", "TargetWidthf", 
        	  "TargetSpeedf", "TargetCoursef", "TargetAccelerationf", "TargetCourseRatef", "TargetDrivingModef", 
        	  "TargetIDs", "TargetClasss", "TargetSensorInformations", "TargetXs", "TargetYs", "TargetDistances",
        	  "TargetLengths", "TargetWidths", "TargetSpeeds", "TargetCourses", "TargetAccelerations", "TargetCourseRates",
        	  "TargetDrivingModes", "NTrajectoryPointsf", "TrajectoryPointITimef", "TrajectoryPointIXf", "TrajectoryPointIYf", 
        	  "NTrajectoryPointss", "TrajectoryPointITimes", "TrajectoryPointIXs", "TrajectoryPointIYs", "T0", "V0", "A0", "T1f", 
        	  "J0f", "S0f", "Cr0f", "T2f", "J1f", "S1f", "Cr1f", "Sn0", "Alpha0", "Delta0", "T1nf", "Jdelta0f", 
        	  "Sdelta0f", "Crdelta0f", "T2nf", "Jdelta1f", "Sdelta1f", "Crdelta1f", "T1s", "J0s", "S0s", "Cr0s", "T2s", 
        	  "J1s", "S1s", "Cr1s", "T1ns", "Jdelta0s", "Sdelta0s", "Crdelta0s", "T2ns", "Jdelta1s", "Sdelta1s", 
        	  "Crdelta1s", "FirstManoeuverTypeLong", "FirstManoeuverTypeLat", "SecondManoeuverTypeLong", 
        	  "SecondManoeuverTypeLat", "TargetSpeed", "TargetLongitudinalAcceleration", "TargetDistanceToPreceedingVehicle"});
        }
    }

}
