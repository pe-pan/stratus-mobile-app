package com.hp.dsg.stratus;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.hp.dsg.stratus.entities.Entity;
import com.hp.dsg.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.hp.dsg.stratus.Mpp.M_STRATUS;

public class OfferingListActivity extends StratusActivity {
    private static final String TAG = OfferingListActivity.class.getSimpleName();

    Spinner categoryFilter;
    private float expandCategoryLineShift;

    private ArrayAdapter adapter;
    private List<Entity> categories;

    private Entity selectedCategory;
    private CharSequence typedText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_offering_list);

        new GetOfferings().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, false);

        ListView list = (ListView) findViewById(R.id.offeringList);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Entity offering = (Entity)parent.getItemAtPosition(position);
                Intent i = new Intent(OfferingListActivity.this, OfferingActivity.class);
                i.putExtra(OfferingActivity.OFFERING_EXTRA_KEY, offering.toJson());
                startActivity(i);
            }
        });

        EditText searchBox = (EditText) findViewById(R.id.searchBox);
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (adapter != null)
                    adapter.getFilter().filter(s);
                typedText = s;
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        final View expandCategoryButton = findViewById(R.id.expandCategoryButton);
        final View hideCategoryButton = findViewById(R.id.hideCategoryButton);
        final View hideCategoryLine = findViewById(R.id.hideCategoryLine);
        categoryFilter = (Spinner) findViewById(R.id.categoryFilter);

        expandCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Expand button clicked");
                if (expandCategoryLineShift == 0) {  // shift not initialized yet
                    expandCategoryLineShift = expandCategoryButton.getX()-hideCategoryButton.getX();
                    hideCategoryLine.setTranslationX(expandCategoryLineShift);
                }
                hideCategoryLine.setVisibility(View.VISIBLE);
                ObjectAnimator oa = ObjectAnimator.ofFloat(hideCategoryLine, "translationX", 0);
                oa.setDuration(600);
                oa.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ObjectAnimator oa = ObjectAnimator.ofFloat(hideCategoryButton, "rotation", 180);
                        oa.setDuration(600);
                        oa.start();
                    }
                });
                oa.start();
            }
        });

        hideCategoryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "Hide button clicked");
                ObjectAnimator oa = ObjectAnimator.ofFloat(hideCategoryLine, "translationX", expandCategoryLineShift);
                oa.setDuration(600);
                oa.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        ObjectAnimator oa = ObjectAnimator.ofFloat(hideCategoryButton, "rotation", 0);
                        oa.setDuration(600);
                        oa.addListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationEnd(Animator animation) {
                                hideCategoryLine.setVisibility(View.GONE);
                            }
                        });
                        oa.start();
                    }
                });
                oa.start();
            }
        });

        categories = getCategories(false); //retrieve cached categories
        final ArrayAdapter categoryAdapter = new ArrayAdapter<>(OfferingListActivity.this, android.R.layout.simple_spinner_item, categories);
        categoryFilter.setAdapter(categoryAdapter);
        categoryFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories.get(position);
                if (adapter != null)
                    adapter.getFilter().filter(typedText);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedCategory = null;
                if (adapter != null)
                    adapter.getFilter().filter(typedText);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_offering, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.offerings) {
            new GetOfferings().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, true); //refresh;
            return true;
        } else return super.onOptionsItemSelected(item);
    }

    private class GetOfferings extends AsyncTask<Boolean, Void, List<Entity>> {

        @Override
        protected List<Entity> doInBackground(Boolean... params) {
            try {
                categories = getCategories(params[0]);
                final List<Entity> offerings = M_STRATUS.getOfferings(params[0]);
                return offerings;
            } catch (Throwable e) {
                showSendErrorDialog(e);
                return null;
            }
        }

        private class ViewHolder {
            TextView name;
            ImageView icon;
        }

        private void updateTitle(int demos) {
            setTitle(getString(R.string.title_activity_offerings)+" ("+demos+" "+(demos == 1 ? getString(R.string.demo) : getString(R.string.demos))+")");
        }

        @Override
        protected void onPostExecute(final List<Entity> offerings) {
            updateTitle(offerings.size());
            final ListView listview = (ListView) findViewById(R.id.offeringList);
            adapter = new ArrayAdapter<Entity>(OfferingListActivity.this,
                    android.R.layout.simple_list_item_1, offerings) {
                private List<Entity> values = offerings;

                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    final View row;
                    ViewHolder holder;
                    if (convertView == null) {
                        LayoutInflater inflater = (OfferingListActivity.this).getLayoutInflater();
                        row = inflater.inflate(R.layout.offering_list_item, parent, false);
                        holder = new ViewHolder();
                        holder.name = (TextView) row.findViewById(R.id.offeringNameList);
                        holder.icon = (ImageView) row.findViewById(R.id.offeringIconList);
                        row.setTag(holder);
                    } else {
                        row = convertView;
                        holder = (ViewHolder) row.getTag();
                    }
                    final Entity offering = values.get(position);

                    holder.name.setText(offering.getProperty("displayName"));
                    setIcon(holder.icon, offering);
                    return row;
                }

                private Filter filter = new Filter() {
                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        FilterResults results = new FilterResults();
                        if (selectedCategory == null && (constraint == null || constraint.length() == 0)) {
                            results.values = offerings;        // return the original (not filtered) list
                            results.count = offerings.size();
                        } else {
                            List<Entity> filteredOfferings = new ArrayList<>();
                            String token = StringUtils.emptifyNullString(constraint).toLowerCase();
                            for (Entity offering : offerings) { // search the original (not filtered) list
                                if (selectedCategory == null || selectedCategory.getProperty("name") == null || // if 'name' property == null, it's the 'all categories' option
                                        selectedCategory.getProperty("name").equals(offering.getProperty("category.name"))) {
                                    if (StringUtils.emptifyNullString(offering.getProperty("displayName")).toLowerCase().contains(token) ||
                                            StringUtils.emptifyNullString(offering.getProperty("description")).toLowerCase().contains(token) ||
                                            StringUtils.emptifyNullString(offering.getProperty("category.displayName")).toLowerCase().contains(token)) {
                                        filteredOfferings.add(offering);
                                    }
                                }
                            }
                            results.values = filteredOfferings;
                            results.count = filteredOfferings.size();
                        }
                        return results;
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {
                        values = (List<Entity>) results.values;
                        updateTitle(values.size());
                        notifyDataSetChanged();
                    }
                };

                @Override
                public Filter getFilter() {
                    return filter;
                }

                @Override
                public int getCount() {
                    return values != null ? values.size() : 0; // in case the async task fails, values might be null
                }

                @Override
                public Entity getItem(int position) {
                    return values.get(position);
                }
            };
            listview.setAdapter(adapter);
            adapter.getFilter().filter(typedText);  // filter the results; what if some typed something in the meantime
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.getSubscriptionsProgress);
            progressBar.setVisibility(View.GONE);
        }
    }
}
