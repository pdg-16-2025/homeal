# Specs

An android application that allows to plan your meals with what you have in your
fridge and pantry.

- Ingredients / products
	- Add / remove custom ingredients / products
	- Input products
		- by scanning barcode
		- manually with a list in the app
	- Support Migros and other grocery stores for products
	- Import / export products / ingredients
	- Expiration dates for products
		- Input manually
	- Once meal is finished, you can add the number of servings leftover
    - Could add manually product in fridge
	
- Recipes
	- Search for recipes
	- Pick recipes and remove ingredients if cooked
	- Plan recipes for the future
	- Add custom recipes
	- Import / export recipes
	- While cooking, you have timers to help you
	- Base recipe each season for everyone (vegan + meat + fish + vege + etc)

- Recommendations
	- Recommend recipes based on ingredients in fridge / pantry
	- Recommend recipes based on expiration dates
	- Recommend recipes based on user similarity preference
	- Recommend recipes based on nutrition needs
	
- Shopping list
	- Plan days on which to grocery shop
	- Generate shopping list based on planned recipes
	- While shopping, you have a list which changes when you scan products
	- Could add manually some product

- Settings
	- Default number of servings
	- Default days for gorcery shopping
	- Ingredients ban list
		- Add / remove ingredients to a ban list so recipes can be filtered
	- Choose vegan and vegetarian options

- Extras if time allows
    - Scan recipes with OCR to input products
    - Scanning while shopping and generating a barcode list which can be used to
    scan products at the self-checkout

# Tech stack

- Kotlin
- Sqlite
- Jetpack Compose
- Android Studio (environment)
