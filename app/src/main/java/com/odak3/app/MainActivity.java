package com.odak3.app;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
    final int BG=Color.rgb(244,245,241), CARD=Color.WHITE, INK=Color.rgb(24,33,29), MUTED=Color.rgb(105,120,112), ACC=Color.rgb(50,108,87);
    LinearLayout content, nav;
    android.content.SharedPreferences sp;
    Handler handler=new Handler();
    int timerSeconds=300, timerTotal=300; boolean running=false; TextView timerText;
    Runnable tick=new Runnable(){ public void run(){ if(running){ timerSeconds--; updateTimer(); if(timerSeconds<=0){ running=false; timerSeconds=timerTotal; xp(5); toast("Odak tamamlandı · +5 XP"); showFocus(); } else handler.postDelayed(this,1000); } }};

    public void onCreate(Bundle b){
        super.onCreate(b); sp=getSharedPreferences("odak3",MODE_PRIVATE);
        getWindow().setStatusBarColor(BG); getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        LinearLayout root=vbox(); root.setBackgroundColor(BG);
        TextView head=txt("ODAK 3",18,true); head.setPadding(dp(18),dp(20),dp(18),dp(10)); root.addView(head);
        ScrollView sv=new ScrollView(this); content=vbox(); content.setPadding(dp(16),0,dp(16),dp(100)); sv.addView(content);
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        nav=new LinearLayout(this); nav.setOrientation(LinearLayout.HORIZONTAL); nav.setPadding(dp(6),dp(6),dp(6),dp(10)); nav.setBackgroundColor(CARD);
        addNav("Bugün",()->showHome()); addNav("Plan",()->showPlan()); addNav("Odak",()->showFocus()); addNav("CBT",()->showCBT()); addNav("Daha",()->showMore());
        root.addView(nav); setContentView(root); showHome();
    }
    void addNav(String s, final Runnable r){ Button b=button(s,false); b.setOnClickListener(v->r.run()); nav.addView(b,new LinearLayout.LayoutParams(0,dp(54),1));}
    LinearLayout vbox(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    TextView txt(String s,int z,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(INK);t.setPadding(0,dp(5),0,dp(5));if(bold)t.setTypeface(null,Typeface.BOLD);return t;}
    Button button(String s,boolean primary){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(primary?Color.WHITE:INK);b.setBackgroundColor(primary?ACC:Color.rgb(231,238,233));return b;}
    EditText input(String hint){EditText e=new EditText(this);e.setHint(hint);e.setTextColor(INK);e.setHintTextColor(MUTED);e.setBackgroundColor(Color.rgb(249,250,248));e.setPadding(dp(12),dp(12),dp(12),dp(12));return e;}
    LinearLayout card(){LinearLayout c=vbox();c.setPadding(dp(16),dp(14),dp(16),dp(14));c.setBackgroundColor(CARD);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(8),0,dp(8));c.setLayoutParams(p);return c;}
    void title(String small,String big){content.addView(txt(small,12,true));TextView h=txt(big,30,true);h.setPadding(0,0,0,dp(12));content.addView(h);}
    void clear(){content.removeAllViews();}
    int dp(int x){return (int)(x*getResources().getDisplayMetrics().density+.5f);}
    void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
    void xp(int n){sp.edit().putInt("xp",sp.getInt("xp",0)+n).apply(); markToday();}
    String day(){return new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date());}
    void markToday(){sp.edit().putBoolean("day_"+day(),true).apply();}

    void showHome(){
        clear(); title("BUGÜN","Her şeyi değil,\nbir sonraki adımı.");
        LinearLayout x=card(); x.addView(txt(sp.getInt("xp",0)+" XP",28,true)); x.addView(txt("Seviye "+(sp.getInt("xp",0)/100+1)+" · Kaçırılan gün için borç yok.",13,false)); content.addView(x);
        LinearLayout tasks=card(); tasks.addView(txt("Bugünün 3'ü",19,true));
        for(int i=1;i<=3;i++){ final int k=i; String val=sp.getString("task"+i,""); if(!val.isEmpty()){CheckBox cb=new CheckBox(this);cb.setText(val);cb.setTextColor(INK);cb.setChecked(sp.getBoolean("taskdone"+i,false));cb.setOnCheckedChangeListener((v,c)->{sp.edit().putBoolean("taskdone"+k,c).apply();if(c)xp(10);});tasks.addView(cb);}}
        Button add=button("+ Hedef ekle",false); add.setOnClickListener(v->taskDialog()); tasks.addView(add);content.addView(tasks);
        LinearLayout rescue=card();rescue.addView(txt("🛟 Şu an kitlendim",19,true));rescue.addView(txt("Karar sayısını azaltıp sadece ilk fiziksel hareketi bul.",14,false));Button rb=button("Beni başlat",true);rb.setOnClickListener(v->rescueDialog());rescue.addView(rb);content.addView(rescue);
        LinearLayout mood=card();mood.addView(txt("Bugün nasılsın?",19,true));LinearLayout mr=new LinearLayout(this);String[] ms={"😣","😕","😐","🙂","✨"};for(int i=0;i<5;i++){final int m=i;Button b=button(ms[i],false);b.setOnClickListener(v->{sp.edit().putInt("mood_"+day(),m).apply();markToday();toast("Kaydedildi");});mr.addView(b,new LinearLayout.LayoutParams(0,dp(52),1));}mood.addView(mr);content.addView(mood);
        LinearLayout tools=card();
        tools.addView(txt("Hızlı araçlar",19,true));
        Button bad=button("🌧 Bugün kötü gidiyor",true); bad.setOnClickListener(v->minimumDayDialog()); tools.addView(bad);
        Button dump=button("🧠 Brain Dump",false); dump.setOnClickListener(v->brainDumpDialog()); tools.addView(dump);
        Button body=button("👥 Body Double başlat",false); body.setOnClickListener(v->bodyDoubleDialog()); tools.addView(body);
        content.addView(tools);
        routines();
    }
    void taskDialog(){
        final EditText e=input("Örn. sunumun girişini hazırla"); final EditText f=input("İlk fiziksel hareket: dosyayı aç");
        LinearLayout l=vbox();l.setPadding(dp(20),0,dp(20),0);l.addView(e);l.addView(f);
        new AlertDialog.Builder(this).setTitle("Yeni küçük hedef").setView(l).setPositiveButton("Ekle",(d,w)->{for(int i=1;i<=3;i++)if(sp.getString("task"+i,"").isEmpty()){sp.edit().putString("task"+i,e.getText().toString()).putString("first"+i,f.getText().toString()).apply();break;}showHome();}).setNegativeButton("Vazgeç",null).show();
    }
    void routines(){
        LinearLayout c=card();c.addView(txt("Minimum rutin",19,true));String[] names={"Su / temel ihtiyaçlar","Günün 3 hedefine bak","Alanı 5 dk toparla","Yarının ilk işini seç"};
        for(int i=0;i<names.length;i++){final int k=i;CheckBox b=new CheckBox(this);b.setText(names[i]);b.setTextColor(INK);b.setChecked(sp.getBoolean("routine_"+day()+"_"+i,false));b.setOnCheckedChangeListener((v,on)->{sp.edit().putBoolean("routine_"+day()+"_"+k,on).apply();if(on)xp(3);});c.addView(b);}content.addView(c);
    }
    void rescueDialog(){
        String[] opts={"Başlayamıyorum","Çok fazla iş var","Telefona takıldım","Motivasyon yok"};
        new AlertDialog.Builder(this).setTitle("ADHD kurtarma modu").setItems(opts,(d,w)->{
            String[] moves={"Görev için gereken ilk şeyi önüne koy. Bitirmeye çalışma.","Sadece bugün sonuç yaratacak tek işi seç. Diğerlerini sonra listesine bırak.","Telefonu kol mesafesinin dışına koy ve çalışma materyalini aç.","İstek gelmesini bekleme. Görevin en mekanik kısmını 5 dakika yap."};
            new AlertDialog.Builder(this).setTitle("Şimdilik sadece bunu yap").setMessage(moves[w]).setPositiveButton("5 dk başla",(x,y)->{setTimer(5);showFocus();}).show();
        }).show();
    }

    void showPlan(){
        clear();title("PLANLAYICI","Zamanı görünür\nhale getir.");
        LinearLayout c=card();c.addView(txt("Bugünün görevleri",19,true));for(int i=1;i<=3;i++){String t=sp.getString("task"+i,"");if(!t.isEmpty())c.addView(txt("• "+t+"\n  İlk adım: "+sp.getString("first"+i,""),15,false));}Button b=button("+ Hedef ekle",false);b.setOnClickListener(v->taskDialog());c.addView(b);content.addView(c);
        LinearLayout later=card();later.addView(txt("Sonra listesi",19,true));EditText e=input("Şimdi yapmayacağım…");later.addView(e);Button a=button("Listeye bırak",false);a.setOnClickListener(v->{String old=sp.getString("later","");sp.edit().putString("later",old+"\n• "+e.getText()).apply();showPlan();});later.addView(a);later.addView(txt(sp.getString("later",""),14,false));content.addView(later);
        LinearLayout blocks=card();blocks.addView(txt("Günün blokları",19,true));
        String[] bn={"🌅 Sabah","☀️ Öğleden sonra","🌙 Akşam"};
        String[] bk={"block_morning","block_afternoon","block_evening"};
        for(int i=0;i<3;i++){final int k=i;EditText be=input(bn[i]+" — bu blokta ne önemli?");be.setText(sp.getString(bk[i],""));be.setOnFocusChangeListener((v,f)->{if(!f)sp.edit().putString(bk[k],((EditText)v).getText().toString()).apply();});blocks.addView(be);}
        content.addView(blocks);
        LinearLayout br=card();br.addView(txt("Akıllı görev parçalama",19,true));br.addView(txt("Büyük görevi fiziksel mikro-adımlara dönüştür. Takılırsan adımı tekrar küçült.",14,false));Button bb=button("Görevi parçala",true);bb.setOnClickListener(v->breakDialog());br.addView(bb);content.addView(br);
        Button carry=button("↪ Bitmeyen görevleri düzenle",false);carry.setOnClickListener(v->carryDialog());content.addView(carry);
    }
    void breakDialog(){EditText e=input("Her satıra bir mikro adım yaz");e.setMinLines(6);new AlertDialog.Builder(this).setTitle("Mikro adımlar").setView(e).setPositiveButton("Kaydet",(d,w)->{sp.edit().putString("breaks",e.getText().toString()).apply();toast("Plan kaydedildi");}).setNegativeButton("Vazgeç",null).show();}

    void setTimer(int m){running=false;handler.removeCallbacks(tick);timerTotal=timerSeconds=m*60;}
    void showFocus(){
        clear();title("ODAK MODU","Tek iş.\nTek pencere.");
        LinearLayout c=card();LinearLayout picks=new LinearLayout(this);int[] mins={5,15,25,45};for(int m:mins){Button b=button(m+" dk",false);b.setOnClickListener(v->{setTimer(m);showFocus();});picks.addView(b,new LinearLayout.LayoutParams(0,dp(50),1));}c.addView(picks);
        timerText=txt("",50,true);timerText.setGravity(Gravity.CENTER);timerText.setPadding(0,dp(30),0,dp(20));c.addView(timerText);
        ProgressBar visual=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);visual.setMax(timerTotal);visual.setProgress(timerTotal-timerSeconds);visual.setMinimumHeight(dp(22));c.addView(visual);updateTimer();
        Button start=button(running?"Duraklat":"Başlat",true);start.setOnClickListener(v->{running=!running;if(running)handler.post(tick);else handler.removeCallbacks(tick);showFocus();});c.addView(start);content.addView(c);
        Button bd=button("👥 Body Double modu",false);bd.setOnClickListener(v->bodyDoubleDialog());content.addView(bd);
        LinearLayout park=card();park.addView(txt("Dikkat park alanı",19,true));EditText e=input("Aklıma geldi, sonra…");park.addView(e);Button p=button("Bırak ve geri dön",false);p.setOnClickListener(v->{String old=sp.getString("later","");sp.edit().putString("later",old+"\n• "+e.getText()).apply();e.setText("");toast("Sonra listesine bırakıldı");});park.addView(p);content.addView(park);
    }
    void updateTimer(){if(timerText!=null)timerText.setText(String.format(Locale.US,"%02d:%02d",timerSeconds/60,timerSeconds%60));}

    void showCBT(){
        clear();title("CBT MERKEZİ","Düşünceyi gerçek\ngibi kabul etme.");
        LinearLayout c=card();c.addView(txt("Yönlendirmeli düşünce kaydı",19,true));c.addView(txt("Amaç pozitif düşünmek değil; daha dengeli ve işe yarar bir bakış bulmak.",14,false));Button b=button("Yeni CBT kaydı",true);b.setOnClickListener(v->cbtDialog());c.addView(b);content.addView(c);
        LinearLayout h=card();h.addView(txt("Son kayıt",19,true));h.addView(txt(sp.getString("cbt_last","Henüz kayıt yok."),14,false));content.addView(h);
    }
    void cbtDialog(){
        LinearLayout l=vbox();l.setPadding(dp(18),0,dp(18),0);
        String[] qs={"Durum — ne oldu?","Otomatik düşünce","Duygu ve yoğunluk (0–10)","Bu düşünceyi destekleyen somut kanıt","Eksik/abartılı olabileceğini gösteren kanıt","Daha dengeli düşünce","Sonraki küçük hareket"};
        EditText[] es=new EditText[qs.length];for(int i=0;i<qs.length;i++){l.addView(txt(qs[i],13,true));es[i]=input(qs[i]);l.addView(es[i]);}
        ScrollView sv=new ScrollView(this);sv.addView(l);
        AlertDialog dlg=new AlertDialog.Builder(this).setTitle("CBT düşünce kaydı").setView(sv).setPositiveButton("Kaydet",null).setNegativeButton("Vazgeç",null).create();
        dlg.setOnShowListener(x->dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{String last="Dengeli düşünce: "+es[5].getText()+"\nSonraki adım: "+es[6].getText();sp.edit().putString("cbt_last",last).apply();xp(10);dlg.dismiss();showCBT();toast("+10 XP");}));dlg.show();
    }


    void brainDumpDialog(){
        EditText e=input("Aklındaki her şeyi buraya dök…"); e.setMinLines(8);
        new AlertDialog.Builder(this).setTitle("🧠 Brain Dump").setMessage("Düzenlemeye çalışma. Önce zihninden çıkar.")
        .setView(e).setPositiveButton("Kaydet",(d,w)->{
            String old=sp.getString("brain_dump","");
            sp.edit().putString("brain_dump",old+"\n"+e.getText().toString()).apply();
            brainSortDialog(e.getText().toString());
        }).setNegativeButton("Vazgeç",null).show();
    }
    void brainSortDialog(String item){
        String[] a={"Bugün","Sonra","Sil"};
        new AlertDialog.Builder(this).setTitle("Bu nereye ait?").setItems(a,(d,w)->{
            if(w==0){ for(int i=1;i<=3;i++) if(sp.getString("task"+i,"").isEmpty()){sp.edit().putString("task"+i,item).putString("first"+i,"İlk 2 dakikalık hareketi yap").apply();break;} }
            if(w==1){String old=sp.getString("later","");sp.edit().putString("later",old+"\n• "+item).apply();}
            toast(w==2?"Bırakıldı":"Yerleştirildi");
            showHome();
        }).show();
    }
    void minimumDayDialog(){
        String[] x={"💧 Temel ihtiyaç: su / yemek / ilaç planını kontrol et","📌 Tek zorunlu işi seç","⚡ Sadece 5 dakika başla"};
        new AlertDialog.Builder(this).setTitle("🌧 Minimum gün modu")
        .setMessage("Normal plan şimdilik önemli değil. Bugünün hedefi günü kurtarmak, telafi etmek değil.\n\n"+String.join("\n\n",x))
        .setPositiveButton("5 dk başla",(d,w)->{setTimer(5);showFocus();})
        .setNegativeButton("Şimdilik burada kal",null).show();
    }
    void bodyDoubleDialog(){
        final EditText goal=input("Bu oturumda tek hedefim…");
        new AlertDialog.Builder(this).setTitle("👥 Body Double").setMessage("Bir çalışma arkadaşı check-in yapıyormuş gibi: tek hedef seç, süreyi başlat, bitince ne kadar ilerlediğini kaydet.")
        .setView(goal).setPositiveButton("25 dk birlikte çalış",(d,w)->{
            sp.edit().putString("body_goal",goal.getText().toString()).apply();setTimer(25);showFocus();
            toast("Tek hedef: "+goal.getText().toString());
        }).setNegativeButton("Vazgeç",null).show();
    }
    void carryDialog(){
        String[] opts={"Yarına taşı","Sonraya bırak","Artık gerekli değil"};
        new AlertDialog.Builder(this).setTitle("↪ Bitmeyen görev").setMessage("Bitmemesi başarısızlık değil. Görevin nereye ait olduğuna karar ver.")
        .setItems(opts,(d,w)->{
            if(w==1){String old=sp.getString("later","");for(int i=1;i<=3;i++){String t=sp.getString("task"+i,"");if(!t.isEmpty()&&!sp.getBoolean("taskdone"+i,false))old+="\n• "+t;}sp.edit().putString("later",old).apply();}
            if(w==2){for(int i=1;i<=3;i++)if(!sp.getBoolean("taskdone"+i,false))sp.edit().remove("task"+i).remove("first"+i).remove("taskdone"+i).apply();}
            toast(w==0?"Yarın yeniden seçebilirsin":w==1?"Sonra listesine taşındı":"Gereksiz görevler bırakıldı");showPlan();
        }).show();
    }
    void lessonDialog(String title){
        String body="Bir ADHD stratejisini küçük bir davranışa çevir:\\n\\n";
        if(title.contains("felci")) body+="Görevi bitirmeyi hedefleme. İlk fiziksel hareketi seç: dosyayı aç, ayakkabıyı giy, tek satır yaz. Sonra 5 dakika.";
        else if(title.contains("Zaman")) body+="Zamanı kafanda tutma. Görsel sayaç kullan, işe başlamadan önce tahmin yap ve bitince gerçekle karşılaştır.";
        else if(title.contains("Erteleme")) body+="Motivasyonun gelmesini beklemek yerine davranışı küçült. 'Sadece 5 dakika' başlangıç bariyerini düşürür.";
        else if(title.contains("Duygu")) body+="1) Duyguyu adlandır. 2) Yoğunluğu 0–10 puanla. 3) Şu anda kontrolündeki en küçük davranışı seç.";
        else body+="Hedef kusursuz sonuç değil, yeterince iyi bir sonraki adımdır. %100 yerine tamamlanabilir %60–80 sürümünü tanımla.";
        new AlertDialog.Builder(this).setTitle(title).setMessage(body).setPositiveButton("5 dk uygula",(d,w)->{setTimer(5);showFocus();}).setNegativeButton("Kapat",null).show();
    }

    void showMore(){
        clear();title("İLERLEME","Seri değil,\ngeri dönüş becerisi.");
        int focus=sp.getInt("focus_total",0);LinearLayout s=card();s.addView(txt("Bu hafta",19,true));s.addView(txt(sp.getInt("xp",0)+" XP toplam",25,true));s.addView(txt("Küçük ilerlemeler de ilerlemedir.",14,false));content.addView(s);
        LinearLayout reward=card();reward.addView(txt("🎁 Dopamin / ödül sistemi",19,true));reward.addView(txt("Ödülü sen belirlersin. XP bir baskı değil, ilerlemeyi görünür kılan işaret.",13,false));
        EditText re=input("Örn. 100 XP → sevdiğim diziden 1 bölüm");re.setText(sp.getString("reward",""));reward.addView(re);Button rs=button("Ödülümü kaydet",false);rs.setOnClickListener(v->{sp.edit().putString("reward",re.getText().toString()).apply();toast("Ödül kaydedildi");});reward.addView(rs);content.addView(reward);
        LinearLayout lesson=card();lesson.addView(txt("📚 5 dakikalık ADHD + CBT araçları",19,true));String[] ls={"ADHD felci: başlangıcı küçült","Zaman körlüğü: zamanı dışsallaştır","Erteleme: motivasyon yerine hareket","Duygu yükselince: dur–adlandır–seç","Mükemmeliyetçilik: yeterince iyi adım"};for(String q:ls){Button lb=button(q,false);lb.setOnClickListener(v->lessonDialog(((Button)v).getText().toString()));lesson.addView(lb);}content.addView(lesson);
        LinearLayout w=card();w.addView(txt("Haftalık sistem bakımı",19,true));EditText a=input("Ne işe yaradı?"),b=input("Nerede sürtünme vardı?"),c=input("Neyi %20 kolaylaştırabilirim?");a.setText(sp.getString("w1",""));b.setText(sp.getString("w2",""));c.setText(sp.getString("w3",""));w.addView(a);w.addView(b);w.addView(c);Button save=button("Kaydet",true);save.setOnClickListener(v->{sp.edit().putString("w1",a.getText().toString()).putString("w2",b.getText().toString()).putString("w3",c.getText().toString()).apply();toast("Kaydedildi");});w.addView(save);content.addView(w);
        LinearLayout safe=card();safe.addView(txt("İlaç & sağlık güvenliği",19,true));safe.addView(txt("Odak 3 tıbbi tanı veya tedavi sağlamaz. İlaç, doz veya kullanım saatini uygulamaya göre değiştirme; reçeteleyen hekimle görüş. Ciddi yan etki, kendine zarar verme düşüncesi veya acil sağlık sorunu varsa profesyonel/acil sağlık desteğine başvur.",13,false));content.addView(safe);
        Button clear=button("Tüm yerel verileri sil",false);clear.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Veriler silinsin mi?").setMessage("Bu işlem geri alınamaz.").setPositiveButton("Sil",(d,x)->{sp.edit().clear().apply();showHome();}).setNegativeButton("Vazgeç",null).show());content.addView(clear);
    }
}
