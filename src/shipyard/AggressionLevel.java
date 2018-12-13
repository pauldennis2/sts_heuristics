/**
 * @author Paul Dennis
 * Jul 10, 2018
 */
package shipyard;

public enum AggressionLevel {
	VERY_PEACEFUL, //Never suffers peace weariness. Bonus during peacetime. Huge penalty for aggressive wars. Moderate defensive war weariness
	PEACEFUL, //Never suffers peace weariness. Moderate penalty for aggressive wars. Small defensive war weariness
	MIDDLE, //Will suffer some peace weariness over time. Small penalty for aggressive wars. No defensive war weariness
	AGGRESSIVE, //Will suffer moderate peace weariness. No penalty for aggressive wars. No defensive war weariness
	VERY_AGGRESSIVE; //Substantial peace weariness. Bonus during wartime. No penalty for wars.
}
