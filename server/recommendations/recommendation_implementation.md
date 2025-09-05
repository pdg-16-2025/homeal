# Goal
The goal is the for the usser to have several different type of recommandations based on preference.

## Types of recommendations

1. Left-Over recommendation
Look out what's left in the fridge and what can we do the most of it.

2. Nutriment based

3. Preference Based recommendations
Add like a questionnaire (health heursitic like weight, hight, sportive etc...)

WITH BlackList


## Left-Over Recommendation

Specific goal:
- Use ALL of Left-Overs
- Minimising the ingredients quantity to buy
- Sort meals by expiration date

Add regime filter.

Take the first meal.

## Nutriment Recommendation

Infos to have and use:
- Weight
- Regime
- 

## Preference Recommendation

Add user-similarity preference to recommand based on similar user preference by finding user that have similar preference and look their recipe best rating.
User can rate recipe (between 0 and 5 with 0.5 step).
Find the recipe and we have similarity score between 2 user that is:
sum of abs(user_rate - reviewer_rate) over the common rated recipes divided by the total number.

It give the average of similarity of common rated recipe.
