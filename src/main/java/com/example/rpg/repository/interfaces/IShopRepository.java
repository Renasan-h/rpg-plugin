package com.example.rpg.repository.interfaces;

import com.example.rpg.dto.ShopCategoryDto;
import com.example.rpg.dto.ShopDto;
import com.example.rpg.dto.ShopItemDto;
import org.bukkit.Material;

import java.util.List;

/**
 * SHOP定義を管理するRepository。
 *
 * <p>
 * config.ymlからSHOP定義を読み込み、DTOへ変換して保持する。
 * また、カテゴリや商品の検索機能を提供することで、
 * 呼び出し側がDTOの内部構造（MapやListなど）へ依存しないよう責務を集約する。
 * </p>
 *
 * <p>
 * 将来的に設定ファイルからPostgreSQLなどの永続ストレージへ移行した場合でも、
 * ServiceやFacadeはRepository経由で取得するだけとなり、
 * データ取得方法の変更をRepository内へ閉じ込められる。
 * </p>
 */
public interface IShopRepository {

    /**
     * 読み込み済みのSHOP定義。
     *
     * <p>
     * config.ymlの内容をDTOへ変換した結果を保持する。
     * Repository内のみが保持し、外部から直接変更しないことを前提とする。
     * </p>
     */
    ShopDto getShop();

    /**
     * 設定ファイルを読み込み {@link ShopDto} を生成し保持する
     */
    void load();

    /**
     * SHOPカテゴリリストを取得する。
     *
     * <p>カテゴリの保持形式をRepository内部へ閉じ込めることで、
     * 将来DB化しても呼び出し側の変更を最小化する。</p>
     *
     * @return カテゴリリスト
     */
    List<ShopCategoryDto> getShopCategoryList();

    /**
     * カテゴリIDからSHOPカテゴリを取得する。
     *
     * <p>カテゴリの保持形式をRepository内部へ閉じ込めることで、
     * 将来DB化しても呼び出し側の変更を最小化する。</p>
     *
     * @param categoryId カテゴリID
     * @return カテゴリ。存在しない場合はnull
     */
    ShopCategoryDto findShopCategoryById(String categoryId);

    /**
     * 表示スロットからSHOPカテゴリを取得する。
     *
     * <p>GUI上の配置とカテゴリ定義の対応はRepositoryが把握する。
     * Facadeは「探し方」ではなく「見つかった後の画面遷移」に集中させる。</p>
     *
     * @param slot GUIスロット番号
     * @return カテゴリ。存在しない場合はnull
     */
    ShopCategoryDto findShopCategoryBySlot(int slot);

    /**
     * アイテム一覧を取得する
     *
     * <p>アイテムのみ全種取得する
     * 将来DB化しても呼び出し側の変更を最小化する。</p>
     *
     * @return SHOP商品リスト
     */
    List<ShopItemDto> getShopItemList();

    /**
     * IDからSHOP商品を取得する。
     *
     * <p>カテゴリの保持形式をRepository内部へ閉じ込めることで、
     * 将来DB化しても呼び出し側の変更を最小化する。</p>
     *
     * @param itemId itemId
     * @return SHOP商品リスト。
     */
    ShopItemDto findShopItemById(String itemId);

    /**
     * カテゴリIDと表示スロットからSHOP商品を取得する。
     *
     * <p>商品探索処理をRepositoryに集約し、FacadeがDTOのMap構造へ依存しないようにする。</p>
     *
     * @param categoryId カテゴリID
     * @param slot       GUIスロット番号
     * @return 商品。存在しない場合はnull
     */
    ShopItemDto findShopItemBySlot(String categoryId, int slot);

    /**
     * Materialから売却可能なSHOP商品を取得する。
     *
     * <p>売却対象の検索はSHOP定義全体を横断するため、
     * ServiceではなくRepositoryに閉じ込める。</p>
     *
     * @param material Bukkit Material
     * @return 売却可能商品。存在しない場合はnull
     */
    ShopItemDto findShopSellableItem(Material material);
    
}