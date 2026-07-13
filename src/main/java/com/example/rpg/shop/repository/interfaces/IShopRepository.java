package com.example.rpg.shop.repository.interfaces;

import com.example.rpg.shop.dto.ShopCategoryDto;
import com.example.rpg.shop.dto.ShopDto;
import com.example.rpg.shop.dto.ShopItemDto;
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
    ShopDto getShopDto();

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
    List<ShopCategoryDto> findCategories();

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
     * 指定カテゴリの商品一覧を取得する。
     *
     * @param categoryId カテゴリID
     * @return 商品一覧
     */
    List<ShopItemDto> findShopItems(String categoryId);

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