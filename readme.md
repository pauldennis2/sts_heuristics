# STS Genetic Algorithm - Introduction

Hello and welcome! This is a just-for-fun project born out of my interest in two things:
1. [Slay the Spire](https://en.wikipedia.org/wiki/Slay_the_Spire), a fun [roguelike](https://en.wikipedia.org/wiki/Roguelike) game based on deckbuilding.
2. [Genetic algorithms](https://en.wikipedia.org/wiki/Genetic_algorithm) which I'll explain more about further on down.

First off, I am not at all an expert on either of these topics, and I'm sure some of my terminology is going to be off. This is really just a fun, learning side-project.

## Explaining Slay the Spire

In Slay the Spire, the hero starts with a deck of basic cards consisting of Strikes that do damage to monsters, and Block/Defend cards that block damage. The cards in your deck represent the actions you can take in combat.

You are presented with a map in the form of a ladder of nodes you climb. At each node, you might find treasure, monsters to fight, or a random event.

In each round of combat, you draw a hand of 5 cards from your deck. Each card has an energy cost associated with it (1 for most basic cards, 2 or 3 for some more powerful cards - and some cards are good because they cost 0 energy). You can then play whatever cards you want. The monsters then take their turn, doing damage to you. Then, back to the player. When the player's deck is empty, the discard pile is shuffled into the draw pile.

After combat, you're offered three cards. You can pick one of these and add it to your deck - usually a good idea at the start, since they are an improvement over your starting cards.

You keep climbing at each node until you fight and defeat the boss. Each level will be progressively harder. Each game is independent, so win or lose, if you start a new game, you'll start over at the beginning.

## Modeling a Simpler Version

In the real Slay the Spire, there are a bunch of complicated strategic choices (which path to take, how to build your deck) and tactical choices (which cards to play in combat, how defensive to play). I wanted to drastically simplify the game, so I completely elimated the tactical element. In my simplified version, the hero just draws and plays a number of cards - they don't have any choices to make about which cards to play, or which monsters to target (there's only one monster in each combat).

In addition, the strategic choices are much simpler. There are only 11 different cards (in the real game, there are over 100). At the end of combat the hero is randomly offered two of the following four options:

1. Increase Maximum hitpoints
2. Add a card to deck
3. Remove worst card in the deck
4. Upgrade the best card that hasn't already been upgraded

If "add a card" is chosen, then we offer 3 cards from the pool.

So at the most basic level a strategy consists of a preference/ranking of the available options. For testing purposes, I started off with purely static strategies that couldn't adjust those preferences. Of course this got boring, so I introduced the AdaptiveStrategy class that can react to various circumstances in the game (more on this later).

The "value" of a given strategy is its average level attainment. There are no bosses in my simple game - the hero just keeps climbing until they fail (hitpoints reduced to 0, or combat "times out" after 30 rounds).

## AdaptiveStrategy

The AdaptiveStrategy class is where things get interesting. Each AdaptiveStrategy has a Map<Condition, Values>.

1. Condition - this expresses a boolean condition based on comparing one value to either another value or a constant. For example, "numCards > 10" will evaluate to true if the number of cards in the hero's deck is greater than 10.
2. Values - this is actually another map that replaces existing preferences for various options if the condition evaluates to true.

These conditions and values are all randomly generated, so most of them will be somewhat non-sensical; the condition might be always true, never true, or (most likely) have no relation to the values it's modifying. Here are some examples of each of those:

1. Always true: "numCards > 0"
2. Never true: "averageBlockPerCard > 7" (even if every card in the deck had block value this couldn't be true)
3. No relation: "averageBlockPerCard > 2" -> "strikeExhaust=0.73". The condition is potentially sensible but has no relation to the result.

(One small optimization project added later on to the project is a function that evaluates conditions over several thousand runs. Any condition that *never* gets triggered is thrown onto a "scrap heap" of unused conditions. When we create new conditions, we check to see if the new random condition is one previously known to be a dud, and if so, try another condition).

Actually "sensible" conditions are quite rare in my experience. One example might be:

"level > maxHp * 0.5" -> "maxHp=1.0"

This condition says, "if the current level is greater than half our maximum hitpoints, we'll prioritize increasing max hitpoints over other options." Note that this doesn't mean this would necessarily be a *successful* condition (i.e. one that would improve performance), just that it has some sensible strategy to it.

## Genetic Algorithm

(Ironically, there is a card called Genetic Algorithm in the latest version of STS, which provides increasing block each time it's played).

This is a genetic algorithm, because it uses various "breeding methods" (detailed below) to attempt to find an optimal solution to the problem at hand (i.e. how to progress as far as possible in the game). There is no "intelligence" at work here; I'm simply creating a bunch of random strategies, taking the ones that are most successful, and tweaking them using various methods. This process repeats over and over again.

### Breeding Methods

As I mentioned above, I'm hardly a genetics expert, so forgive me for the abuse of terms. Here are the terms and meta-strategies that I used:

0. Seeding - this just creates random strategies. The best of these can be put through other breeding methods later.

1. Breeding - this was the first method I developed. The AdaptiveStrategy class (and a few others) implement the Tweakable interface, which means they have a `tweak()` method which will produce a copy of itself but with some random changes or mutations. This is equivalent to asexual reproduction, since there's only one parent. The tweaked strategy is referred to as the child.

2. Deep Breeding - my second method is designed to allow newer strategies to catch up, or old stale strategies to potentially take a "quantum leap" forward. Instead of creating children, we create great-great-grandchildren and run them directly. This saves some computation time since we don't have to run the intervening generations.

3. Cross Breeding - this method actually takes a group of strategies and breeds with two-parent reproduction (randomly combining traits from both parents). It's the most computationally complex (O(n^2)) because it creates children for each pair in the group. So 20 parents put in for cross-breeding would generate 760 children, and 1000 parents would generate almost 2 million children.

One of the later additions to the project was to keep track of how often each breeding method produced improved strategies. All the methods were useful at various times - unsurprisingly, seeding never produced results beyond the first round.

### Optimization Methods

#### Visualizing the Problem

I pictured this problem in the following way: imagine a two-dimensional plane that represents all possible strategies. Every strategy that could exist lives somewhere on this plane. Strategies that are similar to each other are located close together on this plane - so if we imagine a strategy S, which lives at coordinates (20, 25) (to pick an arbitrary point), then a tweak of S, which we could call S' (S prime) might live at (20, 26). If two strategies S and T are crossbred, their child SxT should live somewhere between them on this plane.

Now let's add a third dimension which is each strategy's average attainment. We now have a topographic surface with mountains, valleys, peaks, ridges, etc. Our objective is to explore this space and find the tallest peak. Unfortunately, we're limited in that we don't have a map and we can't just "look around". Also, there are a *large* number of possible strategies - essentially an infinite number. (Because strategies can have any number of conditions, there are an infinite number - but if we limit ourselves to conditions that are possible, (I believe) there are a finite number of strategies).

Given the uncertainty, a big part of my approach is to try to allow as many possibly ways for successful strategies to develop. 

## Things I Learned:

1. Multi-threading - one of the things I did to speed up my breeding process is to have the tests for different strategies run on different threads. This forced me to deal with concurrency issues, and use AtomicLongs and ConcurrentHashMaps for data that multiple threads might be trying to access.

The benefit of multi-threading was immediately obvious; the execution speed up approximately 4-fold, or a bit more (i.e. previously a given set of breeding strategies would take 300 seconds to process, but with multi-threading it would be closer to 70-80 seconds). However, I eventually discovered a large flaw that I was not able to pin down and fix: the results from different trials being run at the same time were being mixed up (despite efforts to eliminate this using `synchronized` code blocks and data structures. This would result in mediocre strategies coming out to the top of the pile with apparently great results; for example, an average attainment of 55.5. But when re-running that strategy in isolation, average attainment might be as low as 15-20. 

2. Reflection - again, I'm not an expert on reflection in Java, but it's a tool that allows us to sort of look at our code from the outside - to find out field names of a class and access those fields. I used reflection in this class inside of the `evaluate()` method in the SingleCondition class, in order to go from the String `numCards` to the `numCards` *field*. 

3. Statistics - I never took a full statistics class, so I was mainly blundering through on my own here. One interesting thing I learned is that generally variance between trials as a function of the number of trials `t` seems to be `k/squareRoot(t)` (or that's how it's worked out in all these cases). I discovered this by running a given strategy (or group of strategies) `n` times twice, and comparing the difference, with values of `n` going from 5 to 150. I then graphed the results in Excel and added trendlines. The first formula I found for the variability of a group of strategies was `1.3367*runCount^-0.504`. I then made a big mistake in assuming that if I ran a trial 500 times, I could expect a variance from its "true" value (i.e. what we'd see if we had the time to run the strategy 10,000 times) to be around 0.058, which is fairly small as a percentage of the attainment numbers (which went into the 50s and 60s). So sure, some strategies might occasionally get lucky and slip through.

What I eventually realized is that while the *average* variance might only be ~0.05, the maximum variance was quite a bit higher. I failed to account for the other side of the [Law of large numbers](https://en.wikipedia.org/wiki/Law_of_large_numbers) - that is, when running a trial tens of thousands of times, some of those results are going to be very unlikely on an individual basis. In other words, if I look at a given strategy that has been run 500 times with an average attainment of 55 and said, "Oh - there's only a 0.1% chance that this strategy *actually* should have an attainment of 54 or 56"... when considering 10,000 such instances, we would expect 10 outliers (and probably an even split between underperformers and overperformers). Unfortunately, my "Hall of Fame" style collection rewards outliers.

This outlier problem is what ultimately stumped me in moving further with this project. I realized that the strategies that were rising to the absolute top of the pack were basically good strategies that had also gotten lucky. To compensate for this, I would need either a lot more processing power (i.e. insist on running strategies 10,000 times each instead of 500) or a more informed statistical approach."# sandbox" 
