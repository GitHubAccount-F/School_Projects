//Used for storing line data and making it portable
import exp from "constants";

export interface CoordinatePairs {
    x1: number; // x coordinate of start point
    y1: number; // y coordinate of start point
    x2: number; // x coordinate of end point
    y2: number; // y coordinate of end point
    color: string; // color of line
    key: string // a unique key for the line
}


export interface Path {
    cost: number; //the overall weight of the path

    start: string; //contains the start

    path:string[]; //contains the path
}
