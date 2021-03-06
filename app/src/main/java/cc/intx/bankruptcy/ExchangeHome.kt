package cc.intx.bankruptcy

import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.widget.SearchView
import kotlinx.android.synthetic.main.activity_exchange_home.*
import android.animation.*
import android.content.Context
import android.view.inputmethod.InputMethodManager
import java.util.*


class ExchangeHome : AppCompatActivity() {

    lateinit var mAdapter: ExchangeSearchAdapter
    lateinit var mLayoutManager: RecyclerView.LayoutManager

    private var selectedCryptoCurrency: CryptoCurrencyInfo? = null

    private lateinit var titleBackgroundAnimator: ValueAnimator
    private lateinit var selectedLayoutHeightAnimator: ValueAnimator
    private lateinit var selectedLayoutColorAnimator: ValueAnimator
    private var selectedLayoutUpdateListener = ValueAnimator.AnimatorUpdateListener { valueAnimator ->
        exchangeSelectedCurrencyLayout.setBackgroundColor(valueAnimator.animatedValue as Int)
        exchangeSelectedCurrencySurfaceView.bgColor = valueAnimator.animatedValue as Int
    }

    private var isSearchFilterWaiting = false
    private var oldQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exchange_home)

        exchangeHomeWrapper.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(exchangeHomeWrapper.windowToken, 0)
        }

        mLayoutManager = LinearLayoutManager(this);
        exSearchResultsRecyclerView.layoutManager = mLayoutManager

        mAdapter = ExchangeSearchAdapter(SharedState.getCryptocurrencyList(), ::selectCryptoCurrency, this)
        exSearchResultsRecyclerView.adapter = mAdapter

        initializeAnimators()

        btnTest.setOnClickListener {
            exchangeSelectedCurrencySurfaceView.doDraw()
        }

        btnTest2.setOnClickListener {
            // TEST TODO DELETE DEBUG
            selectCryptoCurrency(SharedState.getCryptocurrencyList().get(Random().nextInt(5)))
        }

        btnTest3.setOnClickListener {
            // TEST TODO DELETE DEBUG
            exchangeSelectedCurrencySurfaceView.toggleThread()
        }

        exchangeSearchField.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextChange(newText: String?): Boolean {
                if (!isSearchFilterWaiting) {
                    isSearchFilterWaiting = true

                    Handler().postDelayed({
                        if (newText != null) {
                            oldQuery = newText
                            mAdapter.filter(exchangeSearchField.query.toString())
                        }
                        isSearchFilterWaiting = false
                    }, 500)
                }
                return true
            }

            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }
        })
    }

    override fun onWindowFocusChanged(hasFocus:Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if (hasFocus) {
            MiscFunctions.adjustSystemUi(window)
        }
    }

    private fun initializeAnimators() {
        //////////////////////////////////
        // selectedLayoutHeightAnimator //
        selectedLayoutHeightAnimator = ValueAnimator.ofInt(MiscFunctions.convertDpToPx(80, this), MiscFunctions.convertDpToPx(300, this))

        selectedLayoutHeightAnimator.addUpdateListener { valueAnimator ->
            val layoutParams = exchangeSelectedCurrencyLayout.layoutParams
            layoutParams.height = valueAnimator.animatedValue as Int
            exchangeSelectedCurrencyLayout.layoutParams = layoutParams
        }

        ////////////////////////////////
        // titleTextViewBgColorAnimator //
        titleBackgroundAnimator = ValueAnimator.ofObject(ArgbEvaluator(), Color.parseColor("#ffffffff"), Color.parseColor("#00000000"))
        titleBackgroundAnimator.duration = 200

        titleBackgroundAnimator.addUpdateListener { valueAnimator ->
            exchangeHomeTitleBg.setBackgroundColor(valueAnimator.animatedValue as Int)
        }

        //////////////////////////////////
        // selectedLayoutHeightAnimator //
        selectedLayoutHeightAnimator.addListener(object: Animator.AnimatorListener{
            override fun onAnimationStart(animation: Animator?) {
            }
            override fun onAnimationRepeat(animation: Animator?) {
            }
            override fun onAnimationEnd(animation: Animator?) {
            }
            override fun onAnimationCancel(animation: Animator?) {
            }
        })
    }

    fun animateSelection(selection: CryptoCurrencyInfo?) {
        if (selectedCryptoCurrency == null && selection != null) {
            val right = exchangeHomeTitleTextView.measuredWidth - exchangeHomeTitleTextView.right.toFloat() - 15
            val top = exchangeHomeTitleTextView.measuredHeight - exchangeHomeTitleTextView.top.toFloat() - 50
            exchangeHomeTitleTextView.animate().translationX(-right).translationY(-top).scaleX(0.8f).scaleY(0.8f).start()

            titleBackgroundAnimator.start()
            selectedLayoutHeightAnimator.start()

            selectedLayoutColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), Color.parseColor("#ffffff"), selection.color)
            selectedLayoutColorAnimator.addUpdateListener(selectedLayoutUpdateListener)
            selectedLayoutColorAnimator.start()
        } else if (selectedCryptoCurrency != null && selection == null) {
            exchangeHomeTitleTextView.animate().translationX(0f).translationY(0f).scaleX(1f).scaleY(1f).start()

            titleBackgroundAnimator.reverse()
            selectedLayoutHeightAnimator.reverse()

            selectedLayoutColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), selectedCryptoCurrency?.color, Color.parseColor("#ffffff"))
            selectedLayoutColorAnimator.addUpdateListener(selectedLayoutUpdateListener)
            selectedLayoutColorAnimator.start()
        } else if (selectedCryptoCurrency != null && selection != null) {
            selectedLayoutColorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), selectedCryptoCurrency?.color, selection.color)
            selectedLayoutColorAnimator.addUpdateListener(selectedLayoutUpdateListener)
            selectedLayoutColorAnimator.start()
        }

        selectedCryptoCurrency = selection
    }

    fun selectCryptoCurrency(selection: CryptoCurrencyInfo?) {
        animateSelection(selection)
    }
}
