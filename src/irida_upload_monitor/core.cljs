(ns irida-upload-monitor.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [clojure.set]
            [reagent.core :as r] 
            [reagent.dom :as rdom]
            [reagent.dom.server]
            [cljs-http.client :as http]
            [cljs.core.async :refer [<! timeout]]
            [ag-grid-react :as ag-grid]
            [ag-charts-react :as ag-chart]
            [cljs.pprint :refer [pprint]]))

(defonce db (r/atom {}))

(def app-version "v0.1.0")

(defn in? 
  "true if coll contains elem"
  [coll elem]  
  (some #(= elem %) coll))


(defn load-uploaded-sequencing-runs []
  ""
  (go
    (let [response (<! (http/get "data/uploaded_sequencing_runs.json"))]
      (if (vector? (:body response))
        (swap! db assoc :uploaded_sequencing_runs (:body response))))))


(defn load-uploaded-sequencing-run [run-id]
  ""
  (go
    (let [response (<! (http/get (str "data/uploaded_sequencing_runs/" run-id ".json")))]
      (if (vector? (:body response))
        (swap! db assoc-in [:libraries-by-sequencing-run-id run-id] (:body response))))))


(defn get-selected-rows [e]
  "Takes an event e from ag-grid and returns the selected rows as a vector of maps"
  (map #(js->clj (.-data %) :keywordize-keys true)
       (-> e
           .-api
           .getSelectedNodes)))


(defn run-selected [e]
  ""
  (let [previously-selected-run-id (:selected-sequencing-run-id @db)
        currently-selected-run-id (:sequencing_run_id (first (get-selected-rows e)))]
    (do
      (load-uploaded-sequencing-run currently-selected-run-id)
      (swap! db assoc :selected-sequencing-run-id currently-selected-run-id))))


(defn header []
  [:header {:style {:display "grid"
                    :grid-template-columns "repeat(2, 1fr)"
                    :align-items "center"
                    :height "48px"}}
   [:div {:style {:display "grid"
                  :grid-template-columns "repeat(2, 1fr)"
                  :align-items "center"}}
    [:h1 {:style {:font-family "Arial" :color "#004a87" :margin "0px"}} "IRIDA Upload Monitor"][:p {:style {:font-family "Arial" :color "grey" :justify-self "start"}} app-version]]
   [:div {:style {:display "grid" :align-self "center" :justify-self "end"}}
    [:img {:src (str "images/bccdc_logo.svg") :height "48px"}]]])


(defn uploaded-sequencing-runs-table []
  (let [runs (:uploaded_sequencing_runs @db)
        row-data runs]
    [:div {:class "ag-theme-balham"
           :style {}}
     [:> ag-grid/AgGridReact
      {:rowData row-data
       :pagination false
       :rowSelection "single"
       :enableCellTextSelection true
       :onFirstDataRendered #(-> % .-api .sizeColumnsToFit)
       :onSelectionChanged run-selected}
      [:> ag-grid/AgGridColumn {:field "sequencing_run_id" :headerName "Run ID" :minWidth 180 :resizable true :filter "agTextColumnFilter" :floatingFilter true :checkboxSelection true :sortable true :sort "inc"}]
      [:> ag-grid/AgGridColumn {:field "timestamp_upload_started" :headerName "Upload Started" :minWidth 180 :resizable true :filter "agTextColumnFilter" :floatingFilter true :sortable true}]
      [:> ag-grid/AgGridColumn {:field "timestamp_upload_completed" :headerName "Upload Completed" :minWidth 180 :resizable true :filter "agTextColumnFilter" :floatingFilter true :sortable true}]]]))


(defn uploaded-libraries-table []
  (let [currently-selected-run (:selected-sequencing-run-id @db)
        libraries (get-in @db [:libraries-by-sequencing-run-id currently-selected-run] [])
        row-data libraries]
    [:div {:class "ag-theme-balham"
           :style {}}
     [:> ag-grid/AgGridReact
      {:rowData row-data
       :pagination false
       :rowSelection "single"
       :enableCellTextSelection true
       :onFirstDataRendered #(-> % .-api .sizeColumnsToFit)
       :onSelectionChanged #()}
      [:> ag-grid/AgGridColumn {:field "library_id" :headerName "Library ID" :minWidth 180 :resizable true :filter "agTextColumnFilter" :floatingFilter true :sortable true :sort "inc"}]
      [:> ag-grid/AgGridColumn {:field "local_project_id" :headerName "Local Project ID" :minWidth 180 :resizable true :filter "agTextColumnFilter" :floatingFilter true :sortable true}]
      [:> ag-grid/AgGridColumn {:field "local_project_name" :headerName "Local Project Name" :minWidth 180 :resizable true :filter "agTextColumnFilter" :floatingFilter true :sortable true}]
      [:> ag-grid/AgGridColumn {:field "remote_project_id" :headerName "Remote Project ID" :minWidth 180 :resizable true :filter "agTextColumnFilter" :floatingFilter true :sortable true}]
      [:> ag-grid/AgGridColumn {:field "remote_project_name" :headerName "Remote Project Name" :minWidth 180 :resizable true :filter "agTextColumnFilter" :floatingFilter true :sortable true}]]]))


(defn root []
  [:div {:style {:display "grid"
                 :grid-template-columns "1fr"
                 :grid-gap "4px 4px"
                 :height "100%"}} 
   [header]
   [:div {:style {:display "grid"
                  :grid-template-columns "2fr 3fr"
                  :grid-template-rows "1fr"
                  :gap "4px"
                  :height "800px"}}
   [:div {:style {:display "grid"
                  :grid-column "1"
                  :grid-row "1"}}
    [uploaded-sequencing-runs-table]]
   [:div {:style {:display "grid"
                  :grid-column "2"
                  :grid-row "1"
                  :gap "4px"}}
    [uploaded-libraries-table]]]])

(defn main []
  (rdom/render [root] (js/document.getElementById "app"))
  (load-uploaded-sequencing-runs))

(set! (.-onload js/window) main)

(comment

  )
