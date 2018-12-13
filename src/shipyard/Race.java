/**
 * @author Paul Dennis
 * Jul 10, 2018
 */
package shipyard;

import java.util.List;

public class Race {

	AggressionLevel aggressionLevel;
	List<RacePerk> perks;
	MissionType tradition; //i.e., Tradition of Exploration, Science, Military, etc
}

enum RacePerk {
	INDUSTRIOUS, //Extra production
	CYBORG, //No morale
	SUBTERRENEAN, //Bonus population capacity
	CRYSTALLINE, //Better understanding of/interface with Power Crystals?
}
