# Specs

## Overview
An Android application that allows you to plan your meals with what you have in your
fridge and pantry.

## Vision
Help users reduce food waste and decision fatigue by connecting their current fridge inventory with actionable meal plans — fully offline.

## Target Personas
1. Busy Professional
2. Student Cook
3. Family Planner

## MVP Scope (Phase 1)
- Fridge ingredient management (manual entry + basic barcode scan)
- Preloaded and user recipes
- Meal calendar assignment
- Basic shopping list generation
- Expiration notifications
- Recipe suggestions
- Settings (servings, dietary, banned ingredients)

## Detailed Feature Breakdown

### Ingredients / Products
- Add and remove custom ingredients (products)
- Input products
	- by scanning a barcode
	- manually via an in-app list
- Support Migros and other grocery store products
- Import and export ingredients
- Expiration dates for products
	- input manually
- Once a meal is finished, add the number of leftover servings
- Manually add a product to the fridge

### Recipes
- Search for recipes
- Pick recipes and remove used ingredients after cooking
- Plan future recipes
- Add custom recipes
- Import / export recipes
- Use cooking timers while preparing a recipe
- Seasonal base recipes for everyone (vegan, meat, fish, vegetarian, etc.)

### Recommendations
- Recommend recipes based on ingredients in fridge / pantry
- Recommend recipes based on expiration dates
- Recommend recipes based on similar user preferences
- Recommend recipes based on nutritional needs

### Shopping List
- Plan days on which to grocery shop
- Generate a shopping list based on planned recipes
- List updates when you scan products while shopping
- Manually add a product

### Settings
- Default number of servings
- Default days for grocery shopping
- Banned ingredients list
	- Add / remove ingredients so recipes can be filtered
- Enable vegan and vegetarian filters

### Extras if Time Allows
- Scan recipes with OCR to extract products
- Generate a barcode list while shopping that can be used at self-checkout

## User Stories (Sample)
| As a | I want to | So that |
|------|-----------|---------|
| User | Add an item to the fridge | I can do it quickly by scanning a barcode |
| User | Reduce waste | I get recipe suggestions using items expiring soon |
| User | Generate a shopping list | It is automatic and only includes what’s missing |
| User | Plan meals for the week | I reduce daily decision fatigue with suggestions |
| User | Eat my own recipes | I can add custom recipes |

## Tech Stack

- Kotlin
- SQLite
- Jetpack Compose
- Android Studio (environment)
