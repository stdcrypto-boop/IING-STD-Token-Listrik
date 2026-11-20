package com.iingstd.token;

import android.Manifest;
import android.app.*;
import android.os.*;
import android.bluetooth.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {
  LinearLayout root; EditText name,meter,token; Spinner nominal; TextView priceText;
  JSONArray records = new JSONArray(); JSONObject prices = new JSONObject();
  String[] amounts={"5000","10000","15000","20000","50000","100000","200000","500000","1000000"};
  int green=Color.rgb(4,120,87);
  String lastReceipt="";
  String money(String v){try{return "Rp"+String.format(new Locale("id","ID"),"%,d",Long.parseLong(v)).replace(',','.');}catch(Exception e){return "Rp0";}}
  String val(EditText e){return e.getText().toString().trim();}
  void load(){try{records=new JSONArray(getPreferences(0).getString("records","[]"));prices=new JSONObject(getPreferences(0).getString("prices","{}"));}catch(Exception e){}}
  void save(){getPreferences(0).edit().putString("records",records.toString()).putString("prices",prices.toString()).apply();}
  String getPrice(String a){return prices.optString(a,String.valueOf(Long.parseLong(a)+3000));}
  public void onCreate(Bundle b){super.onCreate(b);load();home();}
  LinearLayout panel(){root=new LinearLayout(this);root.setOrientation(1);root.setPadding(24,22,24,20);root.setBackgroundColor(Color.rgb(247,250,248));ScrollView s=new ScrollView(this);s.addView(root);setContentView(s);return root;}
  TextView label(String s,int size){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(Color.rgb(20,50,40));t.setPadding(0,12,0,12);root.addView(t);return t;}
  Button button(String s,Runnable r){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(Color.WHITE);b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(green));root.addView(b);b.setOnClickListener(v->r.run());return b;}
  EditText field(String hint,int type){EditText e=new EditText(this);e.setHint(hint);e.setTextSize(18);e.setInputType(type);e.setSingleLine(true);root.addView(e);return e;}
  void header(String title){label("⚡ IING STD",26).setTypeface(null,Typeface.BOLD);label(title,19);}
  void home(){panel();header("Token Listrik");button("＋ Transaksi Baru",()->form());button("☰ Daftar Harga",()->pricePage());button("◷ Riwayat Transaksi",()->history());label("Data disimpan offline di HP. Nama dan nomor meter pelanggan dapat dipakai kembali.",15);}
  void form(){panel();header("Catat Token Listrik");label("Nama pelanggan",16);name=field("Nama pelanggan",1);label("Nomor meter / ID PLN",16);meter=field("Nomor meter",2);label("Pilih nominal token",16);
    nominal=new Spinner(this);ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Rp5.000","Rp10.000","Rp15.000","Rp20.000","Rp50.000","Rp100.000","Rp200.000","Rp500.000","Rp1.000.000"});nominal.setAdapter(adapter);root.addView(nominal);
    priceText=label("",20);nominal.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){public void onNothingSelected(android.widget.AdapterView<?> p){}public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){priceText.setText("Harga jual: "+money(getPrice(amounts[pos])));}});
    label("Nomor token (20 digit)",16);token=field("Masukkan 20 angka",2);token.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(20)});
    button("Simpan Transaksi",()->store());button("Cari Pelanggan Tersimpan",()->chooseCustomer());button("Kembali",()->home());
  }
  void chooseCustomer(){LinkedHashMap<String,String> map=new LinkedHashMap<>();for(int i=records.length()-1;i>=0;i--){JSONObject o=records.optJSONObject(i);if(o!=null)map.put(o.optString("name"),o.optString("meter"));}
    if(map.isEmpty()){toast("Belum ada pelanggan tersimpan");return;}String[] names=map.keySet().toArray(new String[0]);new AlertDialog.Builder(this).setTitle("Pilih pelanggan").setItems(names,(d,i)->{name.setText(names[i]);meter.setText(map.get(names[i]));}).show();}
  void store(){String t=val(token).replace(" ","");if(val(name).isEmpty()||val(meter).isEmpty()){toast("Isi nama dan nomor meter");return;}if(!t.matches("[0-9]{20}")){toast("Token harus 20 digit angka");return;}
    try{JSONObject o=new JSONObject();String a=amounts[nominal.getSelectedItemPosition()];o.put("name",val(name));o.put("meter",val(meter));o.put("token",t);o.put("amount",a);o.put("price",getPrice(a));o.put("date",new SimpleDateFormat("dd/MM/yyyy HH:mm",Locale.getDefault()).format(new Date()));records.put(o);save();lastReceipt=receipt(o);new AlertDialog.Builder(this).setTitle("Berhasil disimpan").setMessage(lastReceipt).setPositiveButton("Cetak", (d,w)->print()).setNegativeButton("Selesai",(d,w)->home()).show();}catch(Exception e){toast("Gagal menyimpan");}
  }
  String receipt(JSONObject o){return "IING STD TOKEN LISTRIK\n------------------------------\nNama: "+o.optString("name")+"\nNo Meter: "+o.optString("meter")+"\nNominal: "+money(o.optString("amount"))+"\nHarga: "+money(o.optString("price"))+"\nTanggal: "+o.optString("date")+"\nTOKEN:\n"+o.optString("token")+"\n------------------------------\nSimpan nomor token Anda\n";}
  void pricePage(){panel();header("Atur Daftar Harga");for(String a:amounts){label("Token "+money(a),16);EditText e=field("Harga jual",2);e.setText(getPrice(a));button("Simpan harga "+money(a),()->{try{long p=Long.parseLong(val(e));if(p<0)throw new Exception();prices.put(a,String.valueOf(p));save();toast("Harga tersimpan");}catch(Exception x){toast("Masukkan harga yang valid");}});}button("Kembali",()->home());}
  void history(){panel();header("Riwayat Transaksi");EditText search=field("Cari nama / nomor meter",1);LinearLayout list=new LinearLayout(this);list.setOrientation(1);root.addView(list);Runnable render=()->{list.removeAllViews();String q=val(search).toLowerCase();for(int i=records.length()-1;i>=0;i--){JSONObject o=records.optJSONObject(i);if(o==null)continue;if(!(o.optString("name")+" "+o.optString("meter")).toLowerCase().contains(q))continue;Button b=new Button(this);b.setAllCaps(false);b.setText(o.optString("name")+" • "+money(o.optString("amount"))+"\n"+o.optString("date"));list.addView(b);b.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Detail token").setMessage(receipt(o)).setPositiveButton("Cetak",(d,w)->{lastReceipt=receipt(o);print();}).setNeutralButton("Salin Token",(d,w)->{((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("Token PLN",o.optString("token")));toast("Token disalin");}).setNegativeButton("Tutup",null).show());}};search.addTextChangedListener(new android.text.TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int c,int a){}public void onTextChanged(CharSequence s,int st,int before,int count){render.run();}public void afterTextChanged(android.text.Editable e){}});render.run();button("Kembali",()->home());}
  void print(){if(lastReceipt.isEmpty()){toast("Belum ada struk");return;}if(Build.VERSION.SDK_INT>=31&&checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.BLUETOOTH_CONNECT},10);toast("Izinkan Bluetooth lalu tekan Cetak lagi");return;}
    BluetoothAdapter adapter=BluetoothAdapter.getDefaultAdapter();if(adapter==null){toast("Bluetooth tidak tersedia");return;}Set<BluetoothDevice> paired=adapter.getBondedDevices();if(paired.isEmpty()){toast("Pasangkan printer melalui pengaturan Bluetooth dulu");return;}ArrayList<BluetoothDevice> devices=new ArrayList<>(paired);String[] names=new String[devices.size()];for(int i=0;i<names.length;i++)names[i]=devices.get(i).getName();new AlertDialog.Builder(this).setTitle("Pilih printer Bluetooth").setItems(names,(d,i)->{BluetoothDevice device=devices.get(i);String data=lastReceipt;new Thread(()->{BluetoothSocket socket=null;try{socket=device.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));socket.connect();OutputStream out=socket.getOutputStream();out.write(new byte[]{27,64});out.write(data.getBytes("UTF-8"));out.write(new byte[]{10,10,10});out.flush();runOnUiThread(()->toast("Struk terkirim"));}catch(Exception e){runOnUiThread(()->toast("Gagal cetak: "+e.getMessage()));}finally{try{if(socket!=null)socket.close();}catch(Exception ignored){}}}).start();}).show();
  }
  void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}