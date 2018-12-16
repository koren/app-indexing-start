package com.recipe_app.client;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.google.firebase.appindexing.Action;
import com.google.firebase.appindexing.FirebaseAppIndex;
import com.google.firebase.appindexing.FirebaseUserActions;
import com.google.firebase.appindexing.Indexable;
import com.google.firebase.appindexing.builders.Actions;
import com.google.firebase.appindexing.builders.Indexables;

import com.recipe_app.client.Recipe.Note;
import com.recipe_app.client.content_provider.RecipeContentProvider;
import com.recipe_app.client.database.RecipeTable;

import java.util.ArrayList;
import java.util.List;


public class AppIndexingService extends IntentService {

    public AppIndexingService() {
        super("AppIndexingService");
        Log.d("MyApp", "AppIndexingService start");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ArrayList<Indexable> indexableNotes = new ArrayList<>();
        Log.d("MyApp", "AppIndexingService handle");
        for (Recipe recipe : getAllRecipes()) {
            Note note = recipe.getNote();
            if (note != null) {
                Indexable noteToIndex = Indexables.noteDigitalDocumentBuilder()
                        .setName(recipe.getTitle() + " Note")
                        .setText(note.getText())
                        .setUrl(recipe.getNoteUrl())
                        .build();

                Log.d("MyApp", "onHandleIntent: " + note.getText());
                indexableNotes.add(noteToIndex);
            }

            //add actions for all. Not true
//            Action action = getRecipeViewAction(recipe);
//            FirebaseUserActions.getInstance().start(action);
//            FirebaseUserActions.getInstance().end(action);
            indexRecipe(recipe);

        }

        if (indexableNotes.size() > 0) {
            Indexable[] notesArr = new Indexable[indexableNotes.size()];
            notesArr = indexableNotes.toArray(notesArr);

            // batch insert indexable notes into index
            FirebaseAppIndex.getInstance().update(notesArr);
            Log.d("MyApp", "AppIndexingService update");
        }
    }

    private void indexRecipe(Recipe recipe) {
        Indexable recipeToIndex = new Indexable.Builder()
                .setName(recipe.getTitle())
                .setUrl(recipe.getRecipeUrl())
                .setImage(recipe.getPhoto())
                .setDescription(recipe.getDescription())
                .build();

        FirebaseAppIndex.getInstance().update(recipeToIndex);
    }

    private List<Recipe> getAllRecipes() {
        ArrayList recipesList = new ArrayList();
        // TODO: Exercise - access all recipes with their notes from the database here.
        String[] projection = {RecipeTable.ID, RecipeTable.TITLE,
                RecipeTable.DESCRIPTION, RecipeTable.PHOTO,
                RecipeTable.PREP_TIME};
        Uri contentUri = RecipeContentProvider.CONTENT_URI.buildUpon().build();
        try (Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null)) {
            while (cursor.moveToNext()) {
                recipesList.add(Recipe.fromCursor(cursor));
            }
        }
        return recipesList;
    }

    private Action getRecipeViewAction(Recipe recipe) {
        return Actions.newView(recipe.getTitle(), recipe.getRecipeUrl());
    }
}
