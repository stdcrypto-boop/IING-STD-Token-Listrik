package com.iingstd.token;

import android.Manifest;
import android.app.*;
import android.os.*;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.GradientDrawable;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
  LinearLayout root; EditText name,meter,token; Spinner nominal; TextView priceText;
  JSONArray records = new JSONArray(); JSONObject prices = new JSONObject(); JSONArray customers = new JSONArray();
  String[] amounts={"5000","10000","15000","20000","50000","100000","200000","500000","1000000"};
  int green=Color.rgb(4,120,87);
  String lastReceipt="";
  String money(String v){try{return "Rp"+String.format(new Locale("id","ID"),"%,d",Long.parseLong(v)).replace(',','.');}catch(Exception e){return "Rp0";}}
  String val(EditText e){return e.getText().toString().trim();}
  void load(){try{records=new JSONArray(getPreferences(0).getString("records","[]"));prices=new JSONObject(getPreferences(0).getString("prices","{}"));customers=new JSONArray(getPreferences(0).getString("customers","[]"));}catch(Exception e){} migrateCustomers();}
  void save(){getPreferences(0).edit().putString("records",records.toString()).putString("prices",prices.toString()).putString("customers",customers.toString()).apply();}
  int customerIndex(String meterId){for(int i=0;i<customers.length();i++){JSONObject o=customers.optJSONObject(i);if(o!=null&&meterId.equals(o.optString("meter")))return i;}return -1;}
  void saveCustomer(String n,String m){if(n.isEmpty()||m.isEmpty())return;try{JSONObject o=new JSONObject();o.put("name",n);o.put("meter",m);int index=customerIndex(m);if(index>=0)customers.put(index,o);else customers.put(o);save();}catch(Exception ignored){}}
  void migrateCustomers(){if(getPreferences(0).getBoolean("customers_migrated",false))return;for(int i=0;i<records.length();i++){JSONObject o=records.optJSONObject(i);if(o!=null)saveCustomer(o.optString("name"),o.optString("meter"));}getPreferences(0).edit().putBoolean("customers_migrated",true).apply();}
  void deleteCustomer(int index){JSONArray next=new JSONArray();for(int i=0;i<customers.length();i++)if(i!=index)next.put(customers.opt(i));customers=next;save();}

  String getPrice(String a){
    String fallback;
    switch(a){
      case "5000": fallback="9000"; break;
      case "10000": fallback="14000"; break;
      case "15000": fallback="19000"; break;
      case "20000": fallback="24000"; break;
      case "50000": fallback="55000"; break;
      case "100000": fallback="105000"; break;
      case "500000": fallback="510000"; break;
      case "1000000": fallback="1010000"; break;
      default: fallback=String.valueOf(Long.parseLong(a)+3000);
    }
    return prices.optString(a,fallback);
  }
  void applyRequestedPricesOnce(){
    if(getPreferences(0).getBoolean("requested_prices_v2",false))return;
    String[][] requested={{"5000","9000"},{"10000","14000"},{"15000","19000"},{"20000","24000"},{"50000","55000"},{"100000","105000"},{"500000","510000"},{"1000000","1010000"}};
    try{
      for(String[] item:requested)prices.put(item[0],item[1]);
      save();
      getPreferences(0).edit().putBoolean("requested_prices_v2",true).apply();
    }catch(Exception ignored){}
  }
  public void onCreate(Bundle b){super.onCreate(b);load();applyRequestedPricesOnce();home();}
  int dp(int v){return (int)(getResources().getDisplayMetrics().density*v+0.5f);}
  GradientDrawable shape(int color,int radius){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(radius));return d;}
  LinearLayout panel(){
    root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(18),dp(18),dp(18),dp(24));root.setBackgroundColor(Color.rgb(240,247,244));
    ScrollView sc=new ScrollView(this);sc.setFillViewport(true);sc.addView(root);setContentView(sc);return root;
  }
  TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(20,50,40));t.setPadding(0,12,0,12);root.addView(t);return t;}
  Button button(String s,Runnable r){
    Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextSize(16);b.setTypeface(null,Typeface.BOLD);b.setTextColor(Color.WHITE);
    b.setBackground(shape(green,14));b.setElevation(dp(3));b.setPadding(dp(12),dp(12),dp(12),dp(12));
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(56));p.setMargins(0,dp(6),0,dp(8));root.addView(b,p);b.setOnClickListener(v->r.run());return b;
  }
  EditText field(String hint,int type){
    EditText e=new EditText(this);e.setHint(hint);e.setTextSize(18);e.setInputType(type);e.setSingleLine(true);
    GradientDrawable bg=shape(Color.WHITE,12);bg.setStroke(dp(1),Color.rgb(204,224,216));e.setBackground(bg);e.setPadding(dp(14),0,dp(14),0);
    LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(54));p.setMargins(0,dp(3),0,dp(10));root.addView(e,p);return e;
  }
  void header(String title){
    LinearLayout banner=new LinearLayout(this);banner.setOrientation(1);banner.setPadding(dp(20),dp(18),dp(20),dp(18));
    GradientDrawable bg=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{Color.rgb(3,73,57),Color.rgb(5,150,105)});
    bg.setCornerRadius(dp(20));banner.setBackground(bg);banner.setElevation(dp(4));
    LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,-2);bp.setMargins(0,0,0,dp(18));root.addView(banner,bp);
    TextView brand=new TextView(this);brand.setText("⚡ IING STD");brand.setTextColor(Color.WHITE);brand.setTextSize(29);brand.setLetterSpacing(0.07f);brand.setTypeface(Typeface.create("sans-serif-black",Typeface.BOLD));banner.addView(brand);
    TextView subtitle=new TextView(this);subtitle.setText(title);subtitle.setTextColor(Color.rgb(209,250,229));subtitle.setTextSize(17);subtitle.setPadding(0,dp(4),0,0);banner.addView(subtitle);
  }
  void home(){panel();header("Token Listrik");button("＋ Transaksi Baru",()->form());button("👥 Daftar Pelanggan",()->customerPage());button("🖨 Ganti Printer Bluetooth",()->printerSettings());button("☰ Daftar Harga",()->pricePage());button("◷ Riwayat Transaksi",()->history());label("Data disimpan offline di HP. Nama dan nomor meter pelanggan dapat dipakai kembali.",15);}
  void form(){panel();header("Catat Token Listrik");label("Nama pelanggan",16);name=field("Nama pelanggan",1);label("Nomor meter / ID PLN",16);meter=field("Nomor meter",2);label("Pilih nominal token",16);
    nominal=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Rp5.000","Rp10.000","Rp15.000","Rp20.000","Rp50.000","Rp100.000","Rp200.000","Rp500.000","Rp1.000.000"});nominal.setAdapter(adapter);root.addView(nominal);
    priceText=label("",20);nominal.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){priceText.setText("Harga jual: "+money(getPrice(amounts[pos])));}});
    label("Nomor token (20 digit)",16);token=field("Masukkan 20 angka",2);
    token.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(24)});
    token.addTextChangedListener(new android.text.TextWatcher(){
      boolean editing=false;
      public void beforeTextChanged(CharSequence s,int start,int count,int after){}
      public void onTextChanged(CharSequence s,int start,int before,int count){}
      public void afterTextChanged(android.text.Editable e){
        if(editing)return;
        String digits=e.toString().replaceAll("[^0-9]","");
        if(digits.length()>20)digits=digits.substring(0,20);
        StringBuilder formatted=new StringBuilder();
        for(int i=0;i<digits.length();i++){if(i>0&&i%4==0)formatted.append(' ');formatted.append(digits.charAt(i));}
        String result=formatted.toString();
        if(!result.equals(e.toString())){
          int cursor=token.getSelectionStart(),beforeDigits=0;
          for(int i=0;i<Math.min(Math.max(cursor,0),e.length());i++)if(Character.isDigit(e.charAt(i)))beforeDigits++;
          int newCursor=Math.min(result.length(),beforeDigits+(beforeDigits>0?(beforeDigits-1)/4:0));
          editing=true;token.setText(result);token.setSelection(newCursor);editing=false;
        }
      }
    });
    button("📋 TEMPEL TOKEN",()->{
      android.content.ClipboardManager cb=(android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
      if(cb==null||!cb.hasPrimaryClip()){toast("Clipboard kosong");return;}
      ClipData clip=cb.getPrimaryClip();
      if(clip==null||clip.getItemCount()==0){toast("Clipboard kosong");return;}
      CharSequence pasted=clip.getItemAt(0).coerceToText(this);
      String digits=pasted==null?"":pasted.toString().replaceAll("[\\s-]","");
      if(!digits.matches("[0-9]{20}")){toast("Teks yang ditempel harus tepat 20 angka token");return;}
      token.setText(digits);token.setSelection(token.length());
    });
    button("Pilih Pelanggan Tersimpan",()->chooseCustomer());button("Simpan Transaksi",()->store());button("Kembali",()->home());
  }
  void chooseCustomer(){
    if(customers.length()==0){new AlertDialog.Builder(this).setMessage("Belum ada pelanggan. Tambahkan pelanggan terlebih dahulu.").setPositiveButton("Tambah", (d,w)->editCustomer(-1,false)).setNegativeButton("Batal",null).show();return;}
    String[] options=new String[customers.length()];
    for(int i=0;i<options.length;i++){JSONObject o=customers.optJSONObject(i);options[i]=o.optString("name")+" - "+o.optString("meter");}
    new AlertDialog.Builder(this).setTitle("Pilih Pelanggan").setItems(options,(d,i)->{JSONObject o=customers.optJSONObject(i);if(o!=null){name.setText(o.optString("name"));meter.setText(o.optString("meter"));}}).setNegativeButton("Batal",null).show();
  }
  void customerPage(){
    panel();header("Daftar Pelanggan");button("＋ Tambah Pelanggan",()->editCustomer(-1,true));
    if(customers.length()==0)label("Belum ada pelanggan tersimpan.",16);
    for(int i=0;i<customers.length();i++){
      final int index=i;JSONObject o=customers.optJSONObject(i);if(o==null)continue;
      button("👤 "+o.optString("name")+"\\n"+o.optString("meter"),()->new AlertDialog.Builder(this).setTitle(o.optString("name"))
        .setItems(new String[]{"Gunakan untuk transaksi","Edit pelanggan","Hapus pelanggan"},(d,which)->{
          if(which==0){form();name.setText(o.optString("name"));meter.setText(o.optString("meter"));}
          if(which==1)editCustomer(index,true);
          if(which==2)new AlertDialog.Builder(this).setTitle("Hapus pelanggan?")
            .setMessage("Hanya menghapus dari daftar pelanggan. Riwayat transaksi tetap tersimpan.")
            .setPositiveButton("Hapus",(dialog,w)->{deleteCustomer(index);customerPage();})
            .setNegativeButton("Batal",null).show();
        }).show());
    }
    button("Kembali",()->home());
  }
  void editCustomer(int index,boolean returnToList){
    JSONObject old=index>=0?customers.optJSONObject(index):null;
    LinearLayout layout=new LinearLayout(this);layout.setOrientation(1);layout.setPadding(dp(20),dp(8),dp(20),0);
    EditText n=new EditText(this);n.setSingleLine(true);n.setHint("Nama pelanggan");n.setInputType(1);if(old!=null)n.setText(old.optString("name"));layout.addView(n);
    EditText m=new EditText(this);m.setSingleLine(true);m.setHint("Nomor meter / ID PLN");m.setInputType(2);if(old!=null)m.setText(old.optString("meter"));layout.addView(m);
    AlertDialog dialog=new AlertDialog.Builder(this).setTitle(index<0?"Tambah Pelanggan":"Edit Pelanggan").setView(layout)
      .setPositiveButton("Simpan",null).setNegativeButton("Batal",null).create();
    dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(btn->{
      String nv=n.getText().toString().trim(),mv=m.getText().toString().trim();
      if(nv.isEmpty()||mv.isEmpty()){toast("Nama dan nomor meter wajib diisi");return;}
      int existing=customerIndex(mv);
      if(existing>=0&&existing!=index){toast("Nomor meter sudah terdaftar");return;}
      if(index>=0&&old!=null&&!mv.equals(old.optString("meter")))deleteCustomer(index);
      saveCustomer(nv,mv);dialog.dismiss();
      if(returnToList)customerPage();else{if(name!=null&&meter!=null){name.setText(nv);meter.setText(mv);}}
    }));
    dialog.show();
  }
  void store(){String t=val(token).replace(" ","");if(val(name).isEmpty()||val(meter).isEmpty()){toast("Isi nama dan nomor meter");return;}if(!t.matches("[0-9]{20}")){toast("Token harus 20 digit angka");return;}
    try{JSONObject o=new JSONObject();String a=amounts[nominal.getSelectedItemPosition()];o.put("name",val(name));o.put("meter",val(meter));o.put("token",t);o.put("amount",a);o.put("price",getPrice(a));o.put("date",new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault()).format(new Date()));records.put(o);saveCustomer(val(name),val(meter));save();lastReceipt=receipt(o);new AlertDialog.Builder(this).setTitle("Berhasil disimpan").setMessage(lastReceipt).setPositiveButton("Cetak", (d,w)->print()).setNegativeButton("Selesai",(d,w)->home()).show();}catch(Exception e){toast("Gagal menyimpan");}
  }
  String receipt(JSONObject o){return "IING STD TOKEN LISTRIK\n------------------------------\nNama: "+o.optString("name")+"\nNo Meter: "+o.optString("meter")+"\nNominal: "+money(o.optString("amount"))+"\nHarga: "+money(getPrice(o.optString("amount")))+"\nTanggal: "+o.optString("date")+"\nTOKEN:\n"+o.optString("token")+"\n------------------------------\nSimpan nomor token Anda\n";}
  void pricePage(){panel();header("Atur Daftar Harga");for(String a:amounts){label("Token "+money(a),16);EditText e=field("Harga jual",2);e.setText(getPrice(a));button("Simpan harga "+money(a),()->{try{long p=Long.parseLong(val(e));if(p<0)throw new Exception();prices.put(a,String.valueOf(p));save();toast("Harga tersimpan");}catch(Exception x){toast("Masukkan harga yang valid");}});}button("Kembali",()->home());}
  void history(){panel();header("Riwayat Transaksi");EditText search=field("Cari nama / nomor meter",1);LinearLayout list=new LinearLayout(this);list.setOrientation(1);root.addView(list);Runnable render=()->{list.removeAllViews();String q=val(search).toLowerCase();for(int i=records.length()-1;i>=0;i--){JSONObject o=records.optJSONObject(i);if(o==null)continue;if(!(o.optString("name")+" "+o.optString("meter")).toLowerCase().contains(q))continue;Button b=new Button(this);b.setAllCaps(false);b.setText(o.optString("name")+" • "+money(o.optString("amount"))+"\n"+o.optString("date"));list.addView(b);b.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Detail token").setMessage(receipt(o)).setPositiveButton("Cetak Ulang Struk",(d,w)->{lastReceipt=receipt(o);print();}).setNeutralButton("Salin Token",(d,w)->{((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Token PLN",o.optString("token")));toast("Token disalin");}).setNegativeButton("Tutup",null).show());}};search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){render.run();}public void afterTextChanged(android.text.Editable e){}});render.run();button("Kembali",()->home());}
  // Black-and-white lightning emblem printed as ESC/POS raster graphics (58mm compatible).
  void printLogo(OutputStream out) throws Exception {
    final int width=96,height=96;
    Bitmap bitmap=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);
    Canvas canvas=new Canvas(bitmap);canvas.drawColor(Color.WHITE);
    Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);paint.setColor(Color.BLACK);paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(5);
    canvas.drawCircle(48,48,43,paint);
    paint.setStyle(Paint.Style.FILL);
    Path bolt=new Path();bolt.moveTo(54,9);bolt.lineTo(24,53);bolt.lineTo(44,53);bolt.lineTo(38,87);bolt.lineTo(76,39);bolt.lineTo(53,39);bolt.close();canvas.drawPath(bolt,paint);
    int bytesPerRow=width/8;
    out.write(new byte[]{27,97,1,29,118,48,0,(byte)bytesPerRow,0,(byte)(height&255),(byte)(height>>8)});
    for(int y=0;y<height;y++)for(int x=0;x<width;x+=8){
      int bits=0;
      for(int k=0;k<8;k++){int color=bitmap.getPixel(x+k,y);int lum=(Color.red(color)+Color.green(color)+Color.blue(color))/3;if(lum<128)bits|=(128>>k);}
      out.write(bits);
    }
    out.write(10);out.write(new byte[]{27,97,0});bitmap.recycle();
  }
  void styledLine(OutputStream out,String line,int mode,int align) throws Exception {
    out.write(new byte[]{27,97,(byte)align,27,33,(byte)mode});
    out.write((line+"\n").getBytes("UTF-8"));
    out.write(new byte[]{27,33,0,27,97,0});
  }
  void printReceipt(OutputStream out,String data) throws Exception {
    out.write(new byte[]{27,64});
    // Raster logo disabled for printer compatibility.
    // Print text only.
    for(String line:data.split("\\n")){
      if(line.startsWith("IING STD TOKEN")) {
        styledLine(out,"*** IING STD ***",0x08,1);
        styledLine(out,"TOKEN LISTRIK",0x08,1);
      } else if(line.startsWith("Nama: ")){
        styledLine(out,"NAMA PELANGGAN",0x08,0);
        String n=line.substring(6);
        // Bold double-height: long customer names still fit the 58mm width.
        styledLine(out,n,0x18,0);
      } else if(line.equals("TOKEN:")){
        styledLine(out,"NOMOR TOKEN PLN",0x08,1);
      } else if(line.matches("[0-9]{20}")){
        // Double-width, double-height, bold token digits with four-digit groups.
        // Two groups (9 characters including space) fit 58mm at double width.
        styledLine(out,line.substring(0,4)+" "+line.substring(4,8),0x38,1);
        styledLine(out,line.substring(8,12)+" "+line.substring(12,16),0x38,1);
        styledLine(out,line.substring(16,20),0x38,1);
      } else {
        styledLine(out,line,0,0);
      }
    }
    out.write(new byte[]{10,10,10});
    out.flush();
  }
  void openBluetoothSettings(String message){
    new AlertDialog.Builder(this).setTitle("Hubungkan printer DIGIT UP")
      .setMessage(message+"\\n\\n1. Nyalakan printer.\\n2. Buka pengaturan Bluetooth dan pasangkan printer.\\n3. Kembali ke aplikasi, lalu tekan Cetak lagi.\\n\\nTransaksi yang sudah disimpan tetap aman.")
      .setPositiveButton("Buka Pengaturan Bluetooth",(d,w)->{
        try{startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS));}
        catch(Exception e){toast("Tidak bisa membuka pengaturan Bluetooth");}
      }).setNegativeButton("Nanti",null).show();
  }
  void print(){
    if(lastReceipt.isEmpty()){toast("Belum ada struk");return;}
    if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){
      requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},10);
      toast("Izinkan perangkat terdekat lalu coba cetak lagi");return;
    }
    BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
    if(adapter==null){toast("Bluetooth tidak tersedia");return;}
    try{
      if(!adapter.isEnabled()){openBluetoothSettings("Bluetooth HP belum aktif.");return;}
      Set<BluetoothDevice> paired=adapter.getBondedDevices();
      if(paired==null||paired.isEmpty()){openBluetoothSettings("Printer belum dipasangkan.");return;}
      ArrayList<BluetoothDevice> devices=new ArrayList<>(paired);
      String saved=getPreferences(0).getString("printer_mac","");
      for(BluetoothDevice d:devices){
        if(!saved.isEmpty()&&saved.equals(d.getAddress())){sendToPrinter(adapter,d,lastReceipt);return;}
      }
      choosePrinter(adapter,devices);
    }catch(SecurityException e){toast("Izin Bluetooth belum diberikan");}
  }
  void choosePrinter(BluetoothAdapter adapter,ArrayList<BluetoothDevice> devices){
    String[] names=new String[devices.size()];
    for(int i=0;i<devices.size();i++){
      BluetoothDevice d=devices.get(i);
      String n=d.getName();
      names[i]=(n==null||n.isEmpty()?"Perangkat Bluetooth":n)+" ("+d.getAddress()+")";
    }
    new AlertDialog.Builder(this).setTitle("Pilih printer untuk disimpan")
      .setItems(names,(dialog,i)->{
        BluetoothDevice selected=devices.get(i);
        getPreferences(0).edit().putString("printer_mac",selected.getAddress()).apply();
        sendToPrinter(adapter,selected,lastReceipt);
      })
      .setNeutralButton("Pengaturan Bluetooth",(dialog,w)->startActivity(new Intent(android.provider.Settings.ACTION_BLUETOOTH_SETTINGS)))
      .setNegativeButton("Batal",null).show();
  }
  void printerSettings(){
    if(Build.VERSION.SDK_INT>=31 && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){
      requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},10);return;
    }
    BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();
    if(adapter==null){toast("Bluetooth tidak tersedia");return;}
    try{
      Set<BluetoothDevice> paired=adapter.getBondedDevices();
      if(paired==null||paired.isEmpty()){openBluetoothSettings("Belum ada printer yang dipasangkan.");return;}
      choosePrinterOnly(new ArrayList<>(paired));
    }catch(SecurityException e){toast("Izin Bluetooth diperlukan");}
  }
  void choosePrinterOnly(ArrayList<BluetoothDevice> devices){
    String[] names=new String[devices.size()];
    for(int i=0;i<devices.size();i++){BluetoothDevice d=devices.get(i);names[i]=d.getName()+" ("+d.getAddress()+")";}
    new AlertDialog.Builder(this).setTitle("Ganti printer otomatis").setItems(names,(dialog,i)->{
      getPreferences(0).edit().putString("printer_mac",devices.get(i).getAddress()).apply();
      toast("Printer disimpan. Cetakan berikutnya otomatis.");
    }).setNegativeButton("Batal",null).show();
  }
  void sendToPrinter(BluetoothAdapter adapter,BluetoothDevice device,String data){
    toast("Mengirim struk ke printer...");
    new Thread(()->{
      BluetoothSocket socket=null;
      try{
        socket=device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
        socket.connect();
        OutputStream out=socket.getOutputStream();
        // A single receipt is small; flush once, then allow the printer buffer to drain.
        printReceipt(out,data);
        Thread.sleep(1500);
        runOnUiThread(()->new AlertDialog.Builder(this).setTitle("Data cetak terkirim")
          .setMessage("Struk sudah dikirim ke "+device.getName()+". Apakah kertas tercetak dengan benar?")
          .setPositiveButton("Sudah",null)
          .setNegativeButton("Belum",(d,w)->new AlertDialog.Builder(this).setTitle("Periksa printer")
            .setMessage("Periksa kertas, daya printer, dan apakah printer dipakai aplikasi lain. Kamu juga bisa mengganti printer tersimpan.")
            .setPositiveButton("Ganti Printer",(dd,ww)->printerSettings()).setNegativeButton("Tutup",null).show())
          .show());
      }catch(Exception e){
        String message=e.getMessage();
        runOnUiThread(()->new AlertDialog.Builder(this).setTitle("Gagal mengirim struk")
          .setMessage(message==null?"Koneksi printer gagal":message)
          .setPositiveButton("Ganti Printer",(d,w)->printerSettings())
          .setNegativeButton("Tutup",null).show());
      }finally{try{if(socket!=null)socket.close();}catch(Exception ignored){}}
    }).start();
  }
  void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}