package com.knight.rxjavademo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.FlowableEmitter;
import io.reactivex.FlowableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

import static io.reactivex.Observable.create;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    @BindView(R.id.text)
    TextView text;
    @BindView(R.id.flatMap)
    Button flatMap;
    @BindView(R.id.concatMap)
    Button concatMap;
    @BindView(R.id.zip)
    Button zip;
    @BindView(R.id.filte)
    Button filte;
    @BindView(R.id.sample)
    Button sample;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        flowable();
    }


    /**
     * RxJava sample 操作符
     */
    private void sample() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                for (int i = 0; ; i++) {
                    e.onNext(i);
                }
            }
        })
                .subscribeOn(Schedulers.io())//在IO线程中进行网络请求
                .sample(2, TimeUnit.SECONDS)//sample 取样
                .observeOn(AndroidSchedulers.mainThread())//回到主线程处理结果
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "" + integer);
                    }
                });
    }

    /**
     * RxJava filte 操作符
     */
    private void filte() {
        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                for (int i = 0; ; i++) {
                    e.onNext(i);
                }
            }
        })
                .subscribeOn(Schedulers.io())//在io线程中执行耗时操作
                .filter(new Predicate<Integer>() {//添加过滤器
                    @Override
                    public boolean test(Integer integer) throws Exception {
                        return integer % 10 == 0;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())//返回结果在主线程中处理
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Log.d(TAG, "" + integer);
                    }
                });
    }


    /**
     * RxJava concatMap 操作符
     */
    public void concatMap() {
        //创建一个Obsrvable (上游 可观察者)
        create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "Observable -----上游-->>> subscribe");
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).concatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                final List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("I am value  " + integer);
                }
                return Observable.fromIterable(list).delay(10, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.d(TAG, "Observer ----下游-->>> accept" + s);
            }
        });
    }

    /**
     * RxJava flatMap 操作符
     */
    public void flatMap() {
        //创建一个Obsrvable (上游 可观察者)
        create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "Observable -----上游-->>> subscribe");
                e.onNext(1);
                e.onNext(2);
                e.onNext(3);
            }
        }).flatMap(new Function<Integer, ObservableSource<String>>() {
            @Override
            public ObservableSource<String> apply(Integer integer) throws Exception {
                final List<String> list = new ArrayList<>();
                for (int i = 0; i < 3; i++) {
                    list.add("I am value  " + integer);
                }
                return Observable.fromIterable(list).delay(10, TimeUnit.MILLISECONDS);
            }
        }).subscribe(new Consumer<String>() {
            @Override
            public void accept(String s) throws Exception {
                Log.d(TAG, "Observer ----下游-->>> accept" + s);
            }
        });
    }

    /**
     * RxJava zip 操作符
     */
    public void zip() {
        //1.创建一个被观察者（上游）
        Observable<Integer> observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "emit 1");
                e.onNext(1);
//                Thread.sleep(1000);
                Log.d(TAG, "emit 2");
                e.onNext(2);
//                Thread.sleep(1000);
                Log.d(TAG, "emit 3");
                e.onNext(3);
//                Thread.sleep(1000);
                Log.d(TAG, "emit 4");
                e.onNext(4);
//                Thread.sleep(1000);
                Log.d(TAG, "onComplete1");
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io());
        Observable<String> observable2 = create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter<String> e) throws Exception {
                Log.d(TAG, "emit A");
                e.onNext("A");
//                Thread.sleep(1000);
                Log.d(TAG, "emit B");
                e.onNext("B");
//                Thread.sleep(1000);
                Log.d(TAG, "emit C");
                e.onNext("C");
//                Thread.sleep(1000);
                Log.d(TAG, "emit Complete2");
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io());
        //使用zip 操作符
        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {
            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }
        }).subscribe(new Observer<String>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "onSubscribe");
            }

            @Override
            public void onNext(String value) {
                Log.d(TAG, "onNext " + value);
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "error");
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "complete");
            }
        });
    }

    public void ziptwo() {
        Observable observable1 = Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                for (int i = 0; ; i++) {//无限循环
                    e.onNext(i);
                }
            }
        })
                .subscribeOn(Schedulers.io())
                .sample(2, TimeUnit.SECONDS);

        Observable observable2 = Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(ObservableEmitter e) throws Exception {
                e.onNext("A");
            }
        })
                .subscribeOn(Schedulers.io());

        Observable.zip(observable1, observable2, new BiFunction<Integer, String, String>() {

            @Override
            public String apply(Integer integer, String s) throws Exception {
                return integer + s;
            }

        })
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {
                        Log.d(TAG, s);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.w(TAG, throwable);
                    }
                });
    }

    /**
     * RxJava flowable 使用
     */
    public void flowable() {
        Flowable.create(new FlowableOnSubscribe<Integer>() {
            @Override
            public void subscribe(FlowableEmitter<Integer> e) throws Exception {
                Log.d(TAG, "emit 1");
                e.onNext(1);
                Log.d(TAG, "emit 2");
                e.onNext(2);
                Log.d(TAG, "emit 3");
                e.onNext(3);
                Log.d(TAG, "compelete");
                e.onComplete();
            }
        }, BackpressureStrategy.ERROR)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        Log.d(TAG, "onSubscribe");
//                        s.request(Long.MAX_VALUE);
                    }

                    @Override
                    public void onNext(Integer integer) {
                        Log.d(TAG, "onNext " + integer);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.d(TAG, "onError " + t);
                    }

                    @Override
                    public void onComplete() {
                        Log.d(TAG, "onComplete");
                    }
                });
    }

    @OnClick({R.id.text, R.id.flatMap, R.id.concatMap, R.id.zip, R.id.filte, R.id.sample})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.text:
                break;
            case R.id.flatMap:
                flatMap();
                break;
            case R.id.concatMap:
                concatMap();
                break;
            case R.id.zip:
                zip();
                break;
            case R.id.filte:
                filte();
                break;
            case R.id.sample:
                sample();
                break;
        }
    }
}
